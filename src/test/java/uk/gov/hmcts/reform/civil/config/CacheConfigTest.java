package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CacheConfigTest {

    @Test
    void shouldCreateAllCaches() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CacheConfig.class);
        CacheManager cacheManager = context.getBean(CacheManager.class);

        assertNotNull(cacheManager.getCache("userInfoCache"));
        assertNotNull(cacheManager.getCache("accessTokenCache"));
        assertNotNull(cacheManager.getCache("courtVenueCache"));
        assertNotNull(cacheManager.getCache("civilCaseCategoryCache"));
        assertNotNull(cacheManager.getCache("ipRateLimitCache"));
        context.close();
    }
}
