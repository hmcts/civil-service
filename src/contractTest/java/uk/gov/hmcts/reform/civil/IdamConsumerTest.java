package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import io.restassured.RestAssured;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class IdamConsumerTest {

    private static final String ACCESS_TOKEN = "111";
    private static final String IDAM_DETAILS_URL = "/details";
    private static final String IDAM_OPENID_TOKEN_URL = "/o/token";

    @Pact(provider = "Idam_api", consumer = "civil_service")
    public RequestResponsePact executeGetUserDetailsAndGet200(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
            .given("there is a logged in user with an access token")
            .uponReceiving("a GET /details request with the access token")
            .path(IDAM_DETAILS_URL)
            .method(HttpMethod.GET.toString())
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createUserDetailsResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserDetailsAndGet200")
    public void shouldGetUserDetails_WhenGetWithAccessToken(MockServer mockServer) throws JSONException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String responseBody = RestAssured
            .given()
            .headers(headers)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(mockServer.getUrl() + IDAM_DETAILS_URL)
            .then()
            .statusCode(200)
            .and()
            .extract()
            .body()
            .asString();

        JSONObject response = new JSONObject(responseBody);

        assertThat(responseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("id")).isNotBlank();
        assertThat(response.getString("forename")).isNotBlank();
        assertThat(response.getString("surname")).isNotBlank();

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();
    }

    @Pact(provider = "Idam_api", consumer = "civil_service")
    public RequestResponsePact executeGetIdamAccessTokenAndGet200(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        return builder
            .given("there is a valid client ID in Idam")
            .uponReceiving("a POST /o/token request")
            .path(IDAM_OPENID_TOKEN_URL)
            .method(HttpMethod.POST.toString())
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createAuthResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAccessTokenAndGet200")
    public void shouldReceiveAccessTokenAnd200_WhenPostToTokenEndpoint(MockServer mockServer) throws JSONException {
        String responseBody = RestAssured
            .given()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(createRequestBody())
            .log().all(true)
            .when()
            .post(mockServer.getUrl() + IDAM_OPENID_TOKEN_URL)
            .then()
            .statusCode(200)
            .and()
            .extract()
            .asString();

        JSONObject response = new JSONObject(responseBody);

        assertThat(response).isNotNull();
        assertThat(response.getString("access_token")).isNotBlank();
        assertThat(response.getString("refresh_token")).isNotBlank();
        assertThat(response.getString("id_token")).isNotBlank();
        assertThat(response.getString("scope")).isNotBlank();
        assertThat(response.getString("token_type")).isEqualTo("Bearer");
        assertThat(response.getString("expires_in")).isNotBlank();

    }

    private PactDslJsonBody createUserDetailsResponse() {
        PactDslJsonArray array = new PactDslJsonArray().stringValue("caseworker-cmc");

        return new PactDslJsonBody()
            .stringType("id", "123")
            .stringType("email", "civil-solicitor@fake.hmcts.net")
            .stringType("forename", "John")
            .stringType("surname", "Smith")
            .stringType("roles", array.toString());
    }

    private PactDslJsonBody createAuthResponse() {
        return new PactDslJsonBody()
            .stringType("access_token", "some-long-value")
            .stringType("refresh_token", "another-long-value")
            .stringType("scope", "openid roles profile")
            .stringType("id_token", "some-value")
            .stringType("token_type", "Bearer")
            .stringType("expires_in", "12345");
    }

    private static String createRequestBody() {
        return "{\"grant_type\": \"password\","
            + " \"client_id\": \"civil_service\","
            + " \"client_secret\": \"some_client_secret\","
            + " \"redirect_uri\": \"/oauth2redirect\","
            + " \"scope\": \"openid roles profile\","
            + " \"username\": \"username\","
            + " \"password\": \"pwd\"\n"
            + " }";
    }
}
