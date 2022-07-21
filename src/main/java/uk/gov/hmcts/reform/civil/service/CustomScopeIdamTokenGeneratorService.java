package uk.gov.hmcts.reform.civil.service;

import org.springframework.beans.factory.annotation.Autowired;
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
}
