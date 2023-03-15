package uk.gov.hmcts.reform.civil.service;

import uk.gov.hmcts.reform.idam.client.models.UserInfo;

public interface UserService {

    UserInfo getUserInfo(String bearerToken);

    String getAccessToken(String username, String password);
}
