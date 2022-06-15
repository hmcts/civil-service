package uk.gov.hmcts.reform.civil.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;

import static uk.gov.hmcts.reform.idam.client.IdamClient.BEARER_AUTH_TYPE;
import static uk.gov.hmcts.reform.idam.client.IdamClient.OPENID_GRANT_TYPE;

@Service
public class CustomScopeIdamTokenGeneratorService {

    private final IdamApi idamApi;
    private final OAuth2Configuration oauth2Configuration;

    private static final String ADDITIONAL_SCOPES = " search-user create-user manage-user";

    @Autowired
    public CustomScopeIdamTokenGeneratorService(IdamApi idamApi, OAuth2Configuration oauth2Configuration) {
        this.idamApi = idamApi;
        this.oauth2Configuration = oauth2Configuration;
    }

    @Cacheable(value = "accessTokenCache")
    public String getAccessToken(String username, String password) {
        return BEARER_AUTH_TYPE + " " + getAccessTokenResponse(username, password).accessToken;
    }

    public TokenResponse getAccessTokenResponse(String username, String password) {
        return idamApi.generateOpenIdToken(
            new TokenRequest(
                oauth2Configuration.getClientId(),
                oauth2Configuration.getClientSecret(),
                OPENID_GRANT_TYPE,
                oauth2Configuration.getRedirectUri(),
                username,
                password,
                oauth2Configuration.getClientScope() + ADDITIONAL_SCOPES,
                null,
                null
            ));
    }


    /* TODO: remove this if above works
    private final UserIdamTokenGeneratorInfo systemUserIdamInfo;
    private final IdamWebApi idamWebApi;

    public IdamTokenGenerator(UserIdamTokenGeneratorInfo systemUserIdamInfo,
                              IdamWebApi idamWebApi) {
        this.systemUserIdamInfo = systemUserIdamInfo;
        this.idamWebApi = idamWebApi;
    }

    @Cacheable(cacheNames = "idam_sys_user_token_cache_generate", key = "'system_user_token'", sync = true)
    public String generate() {
        return getUserBearerToken(
            systemUserIdamInfo.getUserName(),
            systemUserIdamInfo.getUserPassword()
        );
    }

    @Cacheable(value = "idam_sys_user_token_cache", key = "#username", sync = true)
    public String getUserBearerToken(String username, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", systemUserIdamInfo.getIdamRedirectUrl());
        map.add("client_id", systemUserIdamInfo.getIdamClientId());
        map.add("client_secret", systemUserIdamInfo.getIdamClientSecret());
        map.add("username", username);
        map.add("password", password);
        map.add("scope", systemUserIdamInfo.getIdamScope());
        Token tokenResponse = idamWebApi.token(map);

        return "Bearer " + tokenResponse.getAccessToken();
    }

    @Cacheable(value = "idam_sys_user_user_info_cache", key = "#bearerAccessToken", sync = true)
    public UserInfo getUserInfo(String bearerAccessToken) {
        return idamWebApi.userInfo(bearerAccessToken);
    }
    */
}
