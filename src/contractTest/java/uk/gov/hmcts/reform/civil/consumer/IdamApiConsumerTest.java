package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@MockServerConfig(hostInterface = "localhost", port = "6678")
@TestPropertySource(properties = {
    "idam.api.url=http://localhost:6678",
    "idam.client.id=civil-service",
    "idam.client.secret=client-secret",
    "idam.client.redirect_uri=http://localhost/receiver",
    "idam.client.scope=openid profile roles"
})
public class IdamApiConsumerTest extends BaseContractTest {

    private static final String IDAM_OIDC_PROVIDER = "idamApi_oidc";
    private static final String IDAM_USERS_PROVIDER = "idamApi_users";
    private static final String USER_ID = "24828900-4706-4f88-9fa4-0d8a4e047dc2";
    private static final String USER_EMAIL = "civil-system-user@example.com";
    private static final String USER_FORENAME = "Civil";
    private static final String USER_SURNAME = "Service";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String SCOPE = "openid profile roles";
    private static final String TOKEN_REQUEST_BODY = "client_id=civil-service"
        + "&client_secret=client-secret"
        + "&grant_type=password"
        + "&redirect_uri=http%3A%2F%2Flocalhost%2Freceiver"
        + "&username=civil-system-user%40example.com"
        + "&password=password"
        + "&scope=openid+profile+roles";

    @Autowired
    private IdamClient idamClient;

    @Pact(consumer = "civil_service", provider = IDAM_OIDC_PROVIDER)
    public RequestResponsePact generateOpenIdToken(PactDslWithProvider builder) {
        return builder
            .given("a token is requested")
            .uponReceiving("a password grant token request")
            .path("/o/token")
            .method(HttpMethod.POST.toString())
            .matchHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded.*",
                         MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(TOKEN_REQUEST_BODY, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildTokenResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service", provider = IDAM_OIDC_PROVIDER)
    public RequestResponsePact retrieveUserInfo(PactDslWithProvider builder) {
        return builder
            .given("userinfo is requested")
            .uponReceiving("a userinfo request")
            .path("/o/userinfo")
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildUserInfoResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service", provider = IDAM_USERS_PROVIDER)
    public RequestResponsePact retrieveUserDetails(PactDslWithProvider builder) {
        return builder
            .given("a valid user exists")
            .uponReceiving("a tactical user details request")
            .path("/details")
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildUserDetailsResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(providerName = IDAM_OIDC_PROVIDER, pactMethod = "generateOpenIdToken")
    public void verifyGenerateOpenIdToken() {
        String accessToken = idamClient.getAccessToken(USER_EMAIL, PASSWORD);

        assertThat(accessToken, is(equalTo("Bearer " + ACCESS_TOKEN)));
    }

    @Test
    @PactTestFor(providerName = IDAM_OIDC_PROVIDER, pactMethod = "retrieveUserInfo")
    public void verifyRetrieveUserInfo() {
        UserInfo response = idamClient.getUserInfo(AUTHORIZATION_TOKEN);

        assertThat(response.getUid(), is(equalTo(USER_ID)));
        assertThat(response.getRoles(), is(equalTo(List.of("caseworker-civil"))));
    }

    @Test
    @PactTestFor(providerName = IDAM_USERS_PROVIDER, pactMethod = "retrieveUserDetails")
    public void verifyRetrieveUserDetails() {
        UserDetails response = idamClient.getUserDetails(AUTHORIZATION_TOKEN);

        assertThat(response.getId(), is(equalTo(USER_ID)));
        assertThat(response.getEmail(), is(equalTo(USER_EMAIL)));
        assertThat(response.getFullName(), is(equalTo("Civil Service")));
    }

    private DslPart buildTokenResponse() {
        return LambdaDsl.newJsonBody(root -> root
            .stringType("access_token", ACCESS_TOKEN)
            .stringType("expires_in", "28800")
            .stringType("id_token", "id-token")
            .stringType("refresh_token", "refresh-token")
            .stringType("scope", SCOPE)
            .stringType("token_type", "Bearer")
        ).build();
    }

    private DslPart buildUserInfoResponse() {
        return LambdaDsl.newJsonBody(root -> root
            .stringType("sub", USER_EMAIL)
            .stringType("uid", USER_ID)
            .array("roles", roles -> roles.stringType("caseworker-civil"))
        ).build();
    }

    private DslPart buildUserDetailsResponse() {
        return LambdaDsl.newJsonBody(root -> root
            .stringType("id", USER_ID)
            .stringType("email", USER_EMAIL)
            .stringType("forename", USER_FORENAME)
            .stringType("surname", USER_SURNAME)
            .array("roles", roles -> roles.stringType("caseworker-civil"))
        ).build();
    }
}
