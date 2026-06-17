package uk.gov.hmcts.reform.civil.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import uk.gov.hmcts.reform.civil.config.CaseUserRolesCacheProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseUserRolesCacheServiceTest {

    private static final String CASE_ID = "1234567890";
    private static final String USER_ID = "user-abc-123";
    private static final List<String> ROLES = List.of("[APPLICANTSOLICITORONE]", "[CREATOR]");
    private static final String KEY_PREFIX = "civil:v1:case-user-roles";

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseUserRolesCacheMetrics metrics;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CaseUserRolesCacheService cacheService;
    private CaseUserRolesCacheProperties properties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new CaseUserRolesCacheProperties();
        properties.setEnabled(true);
        properties.setTtlSeconds(30);
        properties.setNegativeTtlSeconds(10);
        properties.setKeyPrefix(KEY_PREFIX);
        properties.setCaffeineMaxSize(10000);

        objectMapper = new ObjectMapper();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService = new CaseUserRolesCacheService(
            properties,
            featureToggleService,
            metrics,
            objectMapper,
            Optional.of(redisTemplate)
        );
    }

    @Nested
    class CacheHit {

        @Test
        void shouldReturnCachedRoles_whenFoundInRedis() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString()))
                .thenReturn("[\"[APPLICANTSOLICITORONE]\",\"[CREATOR]\"]");

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);
            verify(metrics).recordHit(CASE_ID, "redis");
        }

        @Test
        void shouldReturnCachedRoles_whenFoundInCaffeine_afterRedisMiss() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString())).thenReturn(null);

            // First, populate Caffeine by putting a value
            cacheService.put(CASE_ID, USER_ID, ROLES);

            // Now Redis returns null but Caffeine should have it
            when(valueOperations.get(anyString())).thenReturn(null);
            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);
            verify(metrics).recordHit(CASE_ID, "caffeine");
        }
    }

    @Nested
    class CacheMiss {

        @Test
        void shouldReturnEmpty_whenNotFoundInAnyStore() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString())).thenReturn(null);

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isEmpty();
            verify(metrics).recordMiss(CASE_ID);
        }
    }

    @Nested
    class CachePut {

        @Test
        void shouldStoreRoles_withStandardTtl() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            cacheService.put(CASE_ID, USER_ID, ROLES);

            String expectedKey = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":" + USER_ID;
            verify(valueOperations).set(
                eq(expectedKey),
                eq("[\"[APPLICANTSOLICITORONE]\",\"[CREATOR]\"]"),
                eq(Duration.ofSeconds(30))
            );
        }
    }

    @Nested
    class NegativeCache {

        @Test
        void shouldCacheEmptyResults_withShorterTtl() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            cacheService.put(CASE_ID, USER_ID, List.of());

            String expectedKey = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":" + USER_ID;
            verify(valueOperations).set(
                eq(expectedKey),
                eq("[]"),
                eq(Duration.ofSeconds(10))
            );
            verify(metrics).recordNegative(CASE_ID);
        }

        @Test
        void shouldReturnEmptyList_whenNegativeCacheHit() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString())).thenReturn("[]");

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).isEmpty();
        }
    }

    @Nested
    class KillSwitch {

        @Test
        void shouldBypassCache_whenKillSwitchDisabled() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(false);

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isEmpty();
            verify(metrics).recordDisabled();
            verify(valueOperations, never()).get(anyString());
        }

        @Test
        void shouldNotWrite_whenKillSwitchDisabled() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(false);

            cacheService.put(CASE_ID, USER_ID, ROLES);

            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        }
    }

    @Nested
    class Invalidation {

        @Test
        void shouldEvictSpecificKey() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            // Put then evict
            cacheService.put(CASE_ID, USER_ID, ROLES);
            cacheService.evict(CASE_ID, USER_ID);

            String expectedKey = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":" + USER_ID;
            verify(redisTemplate).delete(expectedKey);
            verify(metrics).recordEviction(CASE_ID, "mutation");
        }

        @Test
        void shouldEvictAfterGet_returnsEmpty() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            // Put value
            cacheService.put(CASE_ID, USER_ID, ROLES);

            // Evict
            cacheService.evict(CASE_ID, USER_ID);

            // Caffeine should no longer have it
            when(valueOperations.get(anyString())).thenReturn(null);
            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class KeyIsolation {

        @Test
        void shouldProduceDifferentKeys_forDifferentUsers() {
            String key1 = cacheService.buildKey("getUserCaseRoles", CASE_ID, "user-1");
            String key2 = cacheService.buildKey("getUserCaseRoles", CASE_ID, "user-2");

            assertThat(key1).isNotEqualTo(key2);
            assertThat(key1).startsWith(KEY_PREFIX);
            assertThat(key2).startsWith(KEY_PREFIX);
        }

        @Test
        void shouldProduceDifferentKeys_forDifferentCases() {
            String key1 = cacheService.buildKey("getUserCaseRoles", "case-1", USER_ID);
            String key2 = cacheService.buildKey("getUserCaseRoles", "case-2", USER_ID);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        void shouldIncludeAllContextDimensions_inKey() {
            String key = cacheService.buildKey("getUserCaseRoles", CASE_ID, USER_ID);

            assertThat(key).contains(KEY_PREFIX);
            assertThat(key).contains("getUserCaseRoles");
            assertThat(key).contains(CASE_ID);
            assertThat(key).contains(USER_ID);
        }
    }

    @Nested
    class RedisFallback {

        @Test
        void shouldFallbackToCaffeine_whenRedisThrowsException() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            // Put succeeds to both (Redis throws on get but Caffeine has it)
            cacheService.put(CASE_ID, USER_ID, ROLES);

            // Simulate Redis failure on read
            when(valueOperations.get(anyString()))
                .thenThrow(new RuntimeException("Redis connection refused"));

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);
            verify(metrics).recordFallback(CASE_ID);
            verify(metrics).recordError(eq(CASE_ID), anyString());
        }

        @Test
        void shouldUseCaffeineOnly_whenRedisNotAvailable() {
            CaseUserRolesCacheService caffeineOnlyService = new CaseUserRolesCacheService(
                properties,
                featureToggleService,
                metrics,
                objectMapper,
                Optional.empty()
            );

            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            caffeineOnlyService.put(CASE_ID, USER_ID, ROLES);
            Optional<List<String>> result = caffeineOnlyService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);
            verifyNoInteractions(redisTemplate);
        }
    }

    @Nested
    class ExceptionHandling {

        @Test
        void shouldNotCacheOnRedisWriteFailure_butCaffeineStillWorks() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            org.mockito.Mockito.doThrow(new RuntimeException("Redis write failed"))
                .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

            // Should not throw
            cacheService.put(CASE_ID, USER_ID, ROLES);

            // Caffeine should still have the value
            when(valueOperations.get(anyString())).thenReturn(null);
            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            verify(metrics).recordError(eq(CASE_ID), anyString());
        }
    }
}
