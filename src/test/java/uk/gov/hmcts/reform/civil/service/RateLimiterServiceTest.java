package uk.gov.hmcts.reform.civil.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private RateLimiterService rateLimiterService;

    private static final String CACHE_NAME = "ipRateLimitCache";

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(cacheManager);
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    }

    @Test
    void shouldAllowFirstRequest() {
        String ip = "192.168.1.1";

        when(cache.get(anyString())).thenReturn(null);

        boolean allowed = rateLimiterService.allowRequest(ip, 5, 60);

        assertTrue(allowed);
        verify(cache).put(anyString(), any(AtomicInteger.class));
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        String ip = "192.168.1.1";
        String key = ip + ":5:60";

        AtomicInteger counter = new AtomicInteger(1);
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);

        when(wrapper.get()).thenReturn(counter);
        when(cache.get(key)).thenReturn(wrapper);

        boolean allowed = rateLimiterService.allowRequest(ip, 5, 60);

        assertTrue(allowed);
        assertEquals(2, counter.get());
    }

    @Test
    void shouldRejectRequestWhenLimitExceeded() {
        String ip = "192.168.1.1";
        String key = ip + ":2:60";

        AtomicInteger counter = new AtomicInteger(2);
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);

        when(wrapper.get()).thenReturn(counter);
        when(cache.get(key)).thenReturn(wrapper);

        boolean allowed = rateLimiterService.allowRequest(ip, 2, 60);

        assertFalse(allowed);
        assertEquals(3, counter.get());
    }

    @Test
    void shouldResetCounterAfterExpiry() throws InterruptedException {
        String ip = "192.168.1.1";

        when(cache.get(anyString())).thenReturn(null);

        // First request initializes counter and expiry
        assertTrue(rateLimiterService.allowRequest(ip, 1, 1));

        // Wait for expiry (1 second window)
        Thread.sleep(1100);

        when(cache.get(anyString())).thenReturn(null);

        boolean allowedAfterExpiry = rateLimiterService.allowRequest(ip, 1, 1);

        assertTrue(allowedAfterExpiry);
        verify(cache, atLeastOnce()).evict(anyString());
    }

    @Test
    void shouldAllowRequestWhenCacheIsNull() {
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

        boolean allowed = rateLimiterService.allowRequest("192.168.1.1", 5, 60);

        assertTrue(allowed);
    }
}
