package uk.gov.hmcts.reform.civil.client.idam;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.client.idam.models.AuthenticateUserRequest;
import uk.gov.hmcts.reform.civil.client.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.civil.client.idam.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.civil.client.idam.models.GeneratePinRequest;
import uk.gov.hmcts.reform.civil.client.idam.models.GeneratePinResponse;
import uk.gov.hmcts.reform.civil.client.idam.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.civil.client.idam.models.TokenRequest;
import uk.gov.hmcts.reform.civil.client.idam.models.TokenResponse;
import uk.gov.hmcts.reform.civil.client.idam.models.UserDetails;
import uk.gov.hmcts.reform.civil.client.idam.models.UserInfo;

import java.util.List;

@FeignClient(name = "idam-api", url = "${idam.api.url}", configuration = CoreFeignConfiguration.class)
public interface IdamApi {
    /**
     * Tactical get user details.
     * @deprecated This tactical endpoint is replaced by OpenID /o/userinfo.
     */
    @Deprecated
    @GetMapping("/details")
    UserDetails retrieveUserDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @PostMapping("/pin")
    GeneratePinResponse generatePin(
        GeneratePinRequest requestBody,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @GetMapping(
        value = "/pin",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Response authenticatePinUser(
        @RequestHeader("pin") final String pin,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri,
        @RequestParam("state") final String state
    );

    /**
     * Tactical User Authenticate method.
     *
     * @deprecated IDAM oauth2/authorize endpoint is deprecated
     */
    @Deprecated
    @PostMapping(
        value = "/oauth2/authorize",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    AuthenticateUserResponse authenticateUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation,
        @RequestBody AuthenticateUserRequest authenticateUserRequest
    );

    /**
     * Tactical exchange code for token.
     * @deprecated This tactical endpoint is replaced by OpenID /o/token.
     */
    @Deprecated
    @PostMapping(
        value = "/oauth2/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeCode(
        @RequestBody ExchangeCodeRequest exchangeCodeRequest
    );

    @GetMapping("/o/userinfo")
    UserInfo retrieveUserInfo(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @PostMapping(
        value = "/o/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenResponse generateOpenIdToken(@RequestBody TokenRequest tokenRequest);

    @GetMapping("/api/v1/users/{userId}")
    UserDetails getUserByUserId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
            @PathVariable("userId") String userId
    );

    @GetMapping("/api/v1/users")
    List<UserDetails> searchUsers(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
            @RequestParam("query") final String elasticSearchQuery
    );
}
