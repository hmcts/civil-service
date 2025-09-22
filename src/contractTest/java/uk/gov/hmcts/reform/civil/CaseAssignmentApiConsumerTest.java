package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResponse;

import java.io.IOException;
import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "ccdDataStoreAPI_caseAssignedUserRoles")
@MockServerConfig(hostInterface = "localhost", port = "4452")
public class CaseAssignmentApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/case-users";

    @Autowired
    private CaseAssignmentApi caseAssignmentApi;

    @Pact(consumer = "civil_service")
    public RequestResponsePact getUserRoles(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildGetUserRolesResponsePact(builder);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact removeCaseUserRoles(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildRemoveUserRolesResponsePact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "removeCaseUserRoles")
    public void verifyRemoveUserRoles() {
        CaseAssignmentUserRolesResponse response = caseAssignmentApi.removeCaseUserRoles(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            getRequestEntity()
        );
        assertThat(
            response.getStatusMessage(),
            is(equalTo("REMOVED"))
        );
    }

    @Test
    @PactTestFor(pactMethod = "getUserRoles")
    public void verifyUserRoles() {
        CaseAssignmentUserRolesResource response = caseAssignmentApi.getUserRoles(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            List.of("1583841721773828")
        );
        assertThat(
            response.getCaseAssignmentUserRoles().get(0).getUserId(),
            is(equalTo("userId123"))
        );
    }

    private static CaseAssignmentUserRolesRequest getRequestEntity() {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(
                List.of(CaseAssignmentUserRoleWithOrganisation.builder()
                            .caseDataId("caseDataIdTest")
                            .caseRole("caseRoleTest")
                            .userId("userIdTest")
                            .organisationId("organisationIdTest")
                            .build()))
            .build();
    }

    private RequestResponsePact buildRemoveUserRolesResponsePact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to remove a User Role")
            .path(ENDPOINT)
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.DELETE.toString())
            .body(createJsonObject(getRequestEntity()))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildRemoveUserRolesResponseBody())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private RequestResponsePact buildGetUserRolesResponsePact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to get user roles")
            .path(ENDPOINT)
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .matchQuery("case_ids", "1583841721773828", "1583841721773828")
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildUserRolesResponseBody())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    static DslPart buildRemoveUserRolesResponseBody() {
        return newJsonBody(response ->
                               response
                                   .stringMatcher("status_message", "REMOVED", "REMOVED")
        ).build();
    }

    static DslPart buildUserRolesResponseBody() {
        return newJsonBody(response ->
                               response
                                   .minArrayLike(
                                       "case_users",
                                       1,
                                       caseAssignmentUserRoles -> caseAssignmentUserRoles
                                           .stringType("case_id", "1583841721773828")
                                           .stringType("user_id", "userId123")
                                           .stringType("case_role", "caseRole123")
                                   )
        ).build();
    }
}
