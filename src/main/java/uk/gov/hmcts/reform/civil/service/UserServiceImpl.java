package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final IdamClient idamClient;

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String bearerToken) {
        return idamClient.getUserInfo(bearerToken);
    }

    @Cacheable(value = "accessTokenCache")
    public String getAccessToken(String username, String password) {
        return idamClient.getAccessToken(username, password);
    }
}
