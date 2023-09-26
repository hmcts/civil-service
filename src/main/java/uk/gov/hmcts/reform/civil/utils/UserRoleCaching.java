package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Service
@RequiredArgsConstructor
public class UserRoleCaching {


    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    @Cacheable(cacheNames = "UserCache", cacheManager = "userCacheManager")
    public List<String> getUserRoles(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
        return roles;
    }

    @Bean(name = "userCacheManager")
    @SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager getCacheManager(){
        return new ConcurrentMapCacheManager();
    }

}
