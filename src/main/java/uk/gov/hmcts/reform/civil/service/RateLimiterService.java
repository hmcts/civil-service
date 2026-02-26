package uk.gov.hmcts.reform.civil.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimiterService {

    private final CacheManager cacheManager;

    // Map to store expiry times for different rate limits
    private final ConcurrentHashMap<String, Long> expiryTimes = new ConcurrentHashMap<>();

    // Map to store all registered rate limits (endpointPath -> RateLimitInfo)
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitRegistry = new ConcurrentHashMap<>();

    /**
     * Registers a rate-limited endpoint in the registry.
     */
    public void registerRateLimit(String endpointPath, int limit, int timeWindowSeconds, String description) {
        rateLimitRegistry.put(endpointPath, new RateLimitInfo(limit, timeWindowSeconds, description));
        log.info("register rate limit endpoint : {} limit : {} timeInSeconds : {} description : {} ",
                 endpointPath, limit, timeWindowSeconds, description);
    }

    /**
     * Checks if the request from the given IP should be allowed based on specified rate limits.
     */
    public boolean allowRequest(String ipAddress, int limit, int timeWindowSeconds) {
        Cache cache = cacheManager.getCache("ipRateLimitCache");
        log.info("Cache {} loaded", cache);
        if (cache == null) {
            return true;
        }

        String cacheKey = buildCacheKey(ipAddress, limit, timeWindowSeconds);
        log.info("Cache key generated : {} ", cacheKey);
        checkAndResetExpiredCounter(cache, cacheKey);

        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
        log.info("valueWrapper : {} ", valueWrapper);
        AtomicInteger counter;

        if (valueWrapper == null) {
            // First request in this time window
            counter = new AtomicInteger(1);
            cache.put(cacheKey, counter);
            updateExpiryTime(cacheKey, timeWindowSeconds);

            return true;
        } else {
            // Subsequent request in this time window
            counter = (AtomicInteger) valueWrapper.get();
            int currentCount = counter.incrementAndGet();

            // If this is a new time window, update the expiry time
            if (!expiryTimes.containsKey(cacheKey)) {
                updateExpiryTime(cacheKey, timeWindowSeconds);
            }

            return currentCount <= limit;
        }
    }

    private String buildCacheKey(String ipAddress, int limit, int timeWindowSeconds) {
        return ipAddress + ":" + limit + ":" + timeWindowSeconds;
    }

    private void updateExpiryTime(String cacheKey, int timeWindowSeconds) {
        expiryTimes.put(cacheKey, System.currentTimeMillis() + (timeWindowSeconds * 1000L));
        log.info("expiryTimes size : {} ", expiryTimes.size());
    }

    private void checkAndResetExpiredCounter(Cache cache, String cacheKey) {
        Long expiryTime = expiryTimes.get(cacheKey);
        long currentTime = System.currentTimeMillis();

        if (expiryTime != null && currentTime > expiryTime) {
            // Time window has expired, reset the counter
            cache.evict(cacheKey);
            expiryTimes.remove(cacheKey);
            log.info("expiryTimes size is {} after removing cacheKey: {} ", expiryTimes.size(), cacheKey);
        }
    }

    /**
     * Class to store info about rate limit configuration.
     */
    @Getter
    public static class RateLimitInfo {
        private final int limit;
        private final int timeWindowSeconds;
        private final String description;

        public RateLimitInfo(int limit, int timeWindowSeconds, String description) {
            this.limit = limit;
            this.timeWindowSeconds = timeWindowSeconds;
            this.description = description;
        }

    }
}
