package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.cache.CacheManager;
import javax.cache.Caching;

@TestConfiguration
public class TestCacheConfig {

    @Bean
    public CacheManager testCacheManager() {
        return Caching.getCachingProvider().getCacheManager();
    }
}
