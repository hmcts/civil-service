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

    public static final String READ_ONLY_ENTITY = "ReadOnlyEntity";

    @Bean
    public CacheManager cacheManager() {
        CacheManager cacheManager = Caching.getCachingProvider("com.github.benmanes.caffeine.jcache.CaffeineCachingProvider")
            .getCacheManager();
        configureCache(cacheManager);
        return cacheManager;
    }

    private void configureCache(CacheManager cacheManager) {
        MutableConfiguration<Object, Object> cacheConfig = new MutableConfiguration<>()
            .setStoreByValue(false)
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY));

        cacheManager.createCache(READ_ONLY_ENTITY, cacheConfig);
    }
}

