package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleCaching {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    @Cacheable(cacheNames = "UserCache", cacheManager = "userCacheManager", key = "#userInfo.uid")
    public List<String> getUserRoles(String bearerToken, String ccdCaseRef) {
        UserInfo userInfo = userService.getUserInfo(bearerToken);
        List<String> roles = coreCaseUserService.getUserCaseRoles(ccdCaseRef, userInfo.getUid());
        return roles;
    }

    @Bean(name = "userCacheManager")
    public CacheManager getCacheManager() {
        return new ConcurrentMapCacheManager();
    }

}
