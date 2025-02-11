package uk.gov.hmcts.reform.dashboard.cache;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return Caching.getCachingProvider("com.github.benmanes.caffeine.jcache.CaffeineCachingProvider")
            .getCacheManager();
    }

    @Bean
    public void configureCache(CacheManager cacheManager) {
        MutableConfiguration<Object, Object> cacheConfig = new MutableConfiguration<>()
            .setStoreByValue(false)  // No deep copies, better performance
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY)); // 24-hour cache

        cacheManager.createCache("ReadOnlyEntity", cacheConfig);
    }
}

