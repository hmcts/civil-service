package uk.gov.hmcts.reform.civil.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
public class UserService {

    private final IdamClient idamClient;

    @Autowired
    public UserService(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String bearerToken) {
        return idamClient.getUserInfo(bearerToken);
    }
}
