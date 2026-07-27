package uk.gov.hmcts.reform.civil.service;

import com.google.common.collect.Lists;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamIdamException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String SUB = "user-idam@reform.local";
    private static final String MASKED_SUB = "*********@************";
    private static final String UID = "user-idam-01";
    private static final String NAME = "User IDAM";
    private static final String GIVEN_NAME = "User";
    private static final String FAMILY_NAME = "IDAM";
    private static final List<String> ROLES = Lists.newArrayList("citizen");

    private static final String AUTHORISATION = "Bearer I am a valid token";
    private static final String PASSWORD = "User password";

    private static final UserInfo userInfo = UserInfo.builder()
        .sub(SUB)
        .uid(UID)
        .name(NAME)
        .givenName(GIVEN_NAME)
        .familyName(FAMILY_NAME)
        .roles(ROLES)
        .build();

    @Mock
    private IdamClient idamClient;

    private UserService userService;

    @BeforeEach
    public void setup() {
        userService = new UserService(idamClient, false);
    }

    @Test
    void shouldReturnUserInfo_whenValidAuthToken() {
        when(idamClient.getUserInfo(AUTHORISATION)).thenReturn(userInfo);
        UserInfo found = userService.getUserInfo(AUTHORISATION);

        assertThat(found.getSub()).isEqualTo(SUB);
        assertThat(found.getUid()).isEqualTo(UID);
        assertThat(found.getName()).isEqualTo(NAME);
        assertThat(found.getGivenName()).isEqualTo(GIVEN_NAME);
        assertThat(found.getFamilyName()).isEqualTo(FAMILY_NAME);
        assertThat(found.getRoles()).isEqualTo(ROLES);
    }

    @Test
    void shouldReturnAccessToken_whenValidUserDetailsAreGiven() {
        when(idamClient.getAccessToken(SUB, PASSWORD)).thenReturn(AUTHORISATION);
        String accessToken = userService.getAccessToken(SUB, PASSWORD);

        assertThat(accessToken).isEqualTo(AUTHORISATION);
    }

    @Test
    void shouldReturnUserDetails_whenValidAuthTokenAndResponse() {
        UserDetails expectedUserDetails = UserDetails.builder()
            .email(SUB)
            .build();
        when(idamClient.getUserDetails(AUTHORISATION)).thenReturn(expectedUserDetails);

        var actualUserDetails = userService.getUserDetails(AUTHORISATION);
        assertThat(expectedUserDetails).isEqualTo(actualUserDetails);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithMaskedEmail_whenFailingToParseIdamResponse() {
        String errorMessage = "Email is " + SUB;
        IllegalArgumentException cause = new IllegalArgumentException(errorMessage);
        when(idamClient.getUserDetails(AUTHORISATION)).thenThrow(cause);

        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.getUserDetails(AUTHORISATION)
        );

        assertThat(exception).hasCause(cause);
        assertThat(exception.getMessage()).contains("Email is");
        assertThat(exception.getMessage()).contains(MASKED_SUB);
        assertThat(exception.getMessage()).doesNotContain(SUB);
    }

    @Test
    void shouldRetryOnceAndReturnUserDetails_whenIdamUserDetailsFirstReturnsServerError() {
        UserDetails expectedUserDetails = UserDetails.builder()
            .email(SUB)
            .build();
        when(idamClient.getUserDetails(AUTHORISATION))
            .thenThrow(internalServerError())
            .thenReturn(expectedUserDetails);

        var actualUserDetails = userService.getUserDetails(AUTHORISATION);

        assertThat(actualUserDetails).isEqualTo(expectedUserDetails);
        verify(idamClient, times(2)).getUserDetails(AUTHORISATION);
    }

    @Test
    void shouldThrowUpstreamIdamExceptionWithCause_whenIdamUserDetailsServerErrorRetryFails() {
        FeignException.InternalServerError serverError = internalServerError();
        when(idamClient.getUserDetails(AUTHORISATION)).thenThrow(serverError);

        var exception = assertThrows(
            UpstreamIdamException.class,
            () -> userService.getUserDetails(AUTHORISATION)
        );

        assertThat(exception)
            .hasMessage("IDAM temporarily unavailable")
            .hasCause(serverError);
        verify(idamClient, times(2)).getUserDetails(AUTHORISATION);
    }

    @Test
    void shouldRethrowFeignUnauthorized_whenIdamRejectsToken() {
        FeignException.Unauthorized unauthorized = new FeignException.Unauthorized(
            "Unauthorized",
            request(),
            new byte[]{},
            Collections.emptyMap()
        );
        when(idamClient.getUserDetails(AUTHORISATION)).thenThrow(unauthorized);

        var exception = assertThrows(
            FeignException.Unauthorized.class,
            () -> userService.getUserDetails(AUTHORISATION)
        );

        assertThat(exception).isSameAs(unauthorized);
        verify(idamClient).getUserDetails(AUTHORISATION);
    }

    private FeignException.InternalServerError internalServerError() {
        return new FeignException.InternalServerError(
            "Internal Server Error",
            request(),
            "Internal Server Error".getBytes(StandardCharsets.UTF_8),
            Collections.emptyMap()
        );
    }

    private Request request() {
        return Request.create(
            Request.HttpMethod.GET,
            "/details",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null
        );
    }
}
