package uk.gov.hmcts.reform.civil.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(List.of(
            new CaffeineCache("userInfoCache",
                              Caffeine.newBuilder()
                                  .expireAfterWrite(1, TimeUnit.HOURS)
                                  .build()
            ),
            new CaffeineCache("accessTokenCache",
                              Caffeine.newBuilder()
                                  .expireAfterWrite(1, TimeUnit.HOURS)
                                  .build()
            ),
            new CaffeineCache("courtVenueCache",
                              Caffeine.newBuilder()
                                  .expireAfterWrite(12, TimeUnit.HOURS)
                                  .build()
            )
        ));

        return cacheManager;
    }
}
