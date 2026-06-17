package uk.gov.hmcts.reform.civil.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.CaseUserRolesCacheProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CaseUserRolesCacheService {

    static final String KILL_SWITCH_FLAG = "case-user-roles-cache-enabled";
    private static final String NEGATIVE_MARKER = "[]";
    private static final String STORE_REDIS = "redis";
    private static final String STORE_CAFFEINE = "caffeine";

    private final CaseUserRolesCacheProperties properties;
    private final FeatureToggleService featureToggleService;
    private final CaseUserRolesCacheMetrics metrics;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final Cache<String, String> caffeineCache;
    private final boolean redisAvailable;

    public CaseUserRolesCacheService(
        CaseUserRolesCacheProperties properties,
        FeatureToggleService featureToggleService,
        CaseUserRolesCacheMetrics metrics,
        ObjectMapper objectMapper,
        Optional<StringRedisTemplate> redisTemplate
    ) {
        this.properties = properties;
        this.featureToggleService = featureToggleService;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate.orElse(null);
        this.redisAvailable = this.redisTemplate != null;

        this.caffeineCache = Caffeine.newBuilder()
            .maximumSize(properties.getCaffeineMaxSize())
            .expireAfterWrite(properties.getTtlSeconds(), TimeUnit.SECONDS)
            .build();

        if (!redisAvailable) {
            log.warn("Redis is not available - CaseUserRolesCache will use Caffeine only");
        }
    }

    public boolean isCacheEnabled() {
        return featureToggleService.isFeatureEnabled(KILL_SWITCH_FLAG);
    }

    public Optional<List<String>> get(String caseId, String userId) {
        if (!isCacheEnabled()) {
            log.warn("Case user roles cache is DISABLED via kill switch");
            metrics.recordDisabled();
            auditLog("CACHE_DISABLED", caseId, userId);
            return Optional.empty();
        }

        String key = buildKey("getUserCaseRoles", caseId, userId);
        String value = getFromStore(key, caseId);

        if (value != null) {
            List<String> roles = deserialize(value);
            auditLog("CACHE_HIT", caseId, userId);
            return Optional.of(roles);
        }

        auditLog("CACHE_MISS", caseId, userId);
        metrics.recordMiss(caseId);
        return Optional.empty();
    }

    public void put(String caseId, String userId, List<String> roles) {
        if (!isCacheEnabled()) {
            return;
        }

        String key = buildKey("getUserCaseRoles", caseId, userId);
        String value = serialize(roles);
        long ttl = roles.isEmpty() ? properties.getNegativeTtlSeconds() : properties.getTtlSeconds();

        if (roles.isEmpty()) {
            log.warn("Caching negative (empty) role result for caseId={}, userId={}", caseId, userId);
            metrics.recordNegative(caseId);
        }

        putToStore(key, value, ttl, caseId);
    }

    public void evict(String caseId, String userId) {
        String key = buildKey("getUserCaseRoles", caseId, userId);
        evictKey(key, caseId, "mutation");
    }

    public void evictAllForCase(String caseId) {
        String pattern = properties.getKeyPrefix() + ":getUserCaseRoles:" + caseId + ":*";
        evictByPattern(pattern, caseId);
        metrics.recordEviction(caseId, "mutation");
        auditLog("CACHE_EVICT_ALL", caseId, null);
    }

    private String getFromStore(String key, String caseId) {
        if (redisAvailable) {
            try {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    metrics.recordHit(caseId, STORE_REDIS);
                    return value;
                }
            } catch (Exception e) {
                log.warn("Redis read failed for key={}, falling back to Caffeine: {}", key, e.getMessage());
                metrics.recordError(caseId, "redis_read_" + e.getClass().getSimpleName());
                metrics.recordFallback(caseId);
            }
        }

        String caffeineValue = caffeineCache.getIfPresent(key);
        if (caffeineValue != null) {
            metrics.recordHit(caseId, STORE_CAFFEINE);
        }
        return caffeineValue;
    }

    private void putToStore(String key, String value, long ttlSeconds, String caseId) {
        caffeineCache.put(key, value);

        if (redisAvailable) {
            try {
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            } catch (Exception e) {
                log.warn("Redis write failed for key={}, Caffeine still cached: {}", key, e.getMessage());
                metrics.recordError(caseId, "redis_write_" + e.getClass().getSimpleName());
                metrics.recordFallback(caseId);
            }
        }
    }

    private void evictKey(String key, String caseId, String reason) {
        caffeineCache.invalidate(key);

        if (redisAvailable) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis eviction failed for key={}: {}", key, e.getMessage());
                metrics.recordError(caseId, "redis_evict_" + e.getClass().getSimpleName());
            }
        }

        metrics.recordEviction(caseId, reason);
        auditLog("CACHE_EVICT", caseId, null);
    }

    private void evictByPattern(String pattern, String caseId) {
        caffeineCache.asMap().keySet().stream()
            .filter(k -> k.startsWith(pattern.replace("*", "")))
            .forEach(caffeineCache::invalidate);

        if (redisAvailable) {
            try {
                var keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                log.warn("Redis pattern eviction failed for pattern={}: {}", pattern, e.getMessage());
                metrics.recordError(caseId, "redis_evict_pattern_" + e.getClass().getSimpleName());
            }
        }
    }

    String buildKey(String method, String caseId, String userId) {
        if (userId != null) {
            return properties.getKeyPrefix() + ":" + method + ":" + caseId + ":" + userId;
        }
        return properties.getKeyPrefix() + ":" + method + ":" + caseId;
    }

    private String serialize(List<String> roles) {
        try {
            return objectMapper.writeValueAsString(roles);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize roles", e);
            return NEGATIVE_MARKER;
        }
    }

    private List<String> deserialize(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached roles", e);
            return List.of();
        }
    }

    private void auditLog(String operation, String caseId, String userId) {
        if (userId != null) {
            log.info("CaseUserRolesCache operation={} caseId={} userId={}", operation, caseId, userId);
        } else {
            log.info("CaseUserRolesCache operation={} caseId={}", operation, caseId);
        }
    }
}
