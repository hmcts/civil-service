package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleCaching {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    @Cacheable(cacheNames = "UserCache", cacheManager = "userCacheManager", key = "#keyToken")
    public List<String> getUserRoles(String bearerToken, String ccdCaseRef, String keyToken) {
        UserInfo userInfo = userService.getUserInfo(bearerToken);
        return coreCaseUserService.getUserCaseRoles(ccdCaseRef, userInfo.getUid());
    }

    public String getCacheKeyToken(String bearerToken) {
        return bearerToken.substring(bearerToken.length() - 16);
    }

    @Bean(name = "userCacheManager")
    public CacheManager getCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @CacheEvict(value = "UserCache", allEntries = true)
    @Scheduled(fixedRateString = "1800000")
    public void emptyUserCache() {
        log.debug("Cache removed");
    }
}
