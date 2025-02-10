package uk.gov.hmcts.reform.dashboard.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        CacheConfigurationBuilder<SimpleKey, Double> configuration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(SimpleKey.class,
                                          Double.class,
                                          ResourcePoolsBuilder.heap(2).offheap(10, MemoryUnit.MB))
            .withExpiry(ExpiryPolicy.NO_EXPIRY);

        cacheManager.createCache("scenario", Eh107Configuration.fromEhcacheCacheConfiguration(configuration));

        return cacheManager;
    }
}
