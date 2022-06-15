package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomScopeIdamTokenGeneratorServiceTest {

    private static final String SUB = "user-idam@reform.local";

    private static final String AUTHORISATION = "Bearer I am a valid token";
    private static final String AUTHORISATION_FROM_RESPONSE = "I am a valid token";
    private static final String PASSWORD = "User password";

    private static final TokenResponse tokenResponse = new TokenResponse(
        AUTHORISATION_FROM_RESPONSE, "expiresIn", "idToken", "refresh", "create-user", "tokenType");

    @Mock
    private IdamApi idamApi;
    @Mock
    private OAuth2Configuration oauth2Configuration;
    private CustomScopeIdamTokenGeneratorService tokenGenerator;

    @BeforeEach
    public void setup() {
        tokenGenerator = new CustomScopeIdamTokenGeneratorService(idamApi, oauth2Configuration);
    }

    @Test
    void shouldReturnAccessToken_whenValidUserDetailsAreGiven() {
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String accessToken = tokenGenerator.getAccessToken(SUB, PASSWORD);

        assertThat(accessToken).isEqualTo(AUTHORISATION);
    }
}
