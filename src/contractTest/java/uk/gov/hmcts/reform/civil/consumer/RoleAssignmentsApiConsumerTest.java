package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.ras.model.GrantType;
import uk.gov.hmcts.reform.civil.ras.model.QueryRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignment;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleCategory;
import uk.gov.hmcts.reform.civil.ras.model.RoleRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleType;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "am_role_assignment_service")
@MockServerConfig(hostInterface = "localhost", port = "6681")
@TestPropertySource(properties = "role-assignment-service.api.url=http://localhost:6681")
public class RoleAssignmentsApiConsumerTest extends BaseContractTest {

    private static final String ACTOR_ID = "11111111-1111-1111-1111-111111111111";
    private static final String CASE_ID = "1712345678901234";
    private static final String ROLE_NAME = "hearing-manager";
    private static final String ROLE_LABEL = "Hearing Manager";
    private static final String ROLE_TYPE = "ORGANISATION";
    private static final String ROLE_CATEGORY = "SYSTEM";
    private static final String GRANT_TYPE = "STANDARD";
    private static final String CLASSIFICATION = "PUBLIC";
    private static final String ASSIGNER_ID = "22222222-2222-2222-2222-222222222222";
    private static final String ROLE_REFERENCE = "civil-hearings-system-user";
    private static final String ROLE_PROCESS = "civil-system-user";

    @Autowired
    private RoleAssignmentsService roleAssignmentsService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getRoleAssignmentsForActor(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a get role assignments by actor id request")
            .path("/am/role-assignments/actors/" + ACTOR_ID)
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildRoleAssignmentResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact queryRoleAssignmentsWithLabels(PactDslWithProvider builder)
        throws JSONException, IOException {
        return builder
            .uponReceiving("a query role assignments with labels request")
            .path("/am/role-assignments/query")
            .matchQuery("includeLabels", "true|false", "true")
            .headers(
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
            )
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(buildActorRoleQueryRequest()))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildRoleAssignmentResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact queryRoleAssignmentsByCaseIdAndRole(PactDslWithProvider builder)
        throws JSONException, IOException {
        return builder
            .uponReceiving("a query role assignments by case id and role request")
            .path("/am/role-assignments/query")
            .matchQuery("includeLabels", "true|false", "true")
            .headers(
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                "sort", "roleName"
            )
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(buildCaseRoleQueryRequest()))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildRoleAssignmentResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact createRoleAssignment(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a create role assignment request")
            .path("/am/role-assignments")
            .headers(
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
            )
            .method(HttpMethod.POST.toString())
            .body(buildCreateRoleAssignmentRequest())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildCreateRoleAssignmentResponse())
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getRoleAssignmentsForActor")
    public void verifyGetRoleAssignmentsForActor() {
        RoleAssignmentServiceResponse response = roleAssignmentsService.getRoleAssignments(ACTOR_ID, AUTHORIZATION_TOKEN);

        assertThat(response.getRoleAssignmentResponse().size(), is(equalTo(1)));
        assertThat(response.getRoleAssignmentResponse().get(0).getActorId(), is(equalTo(ACTOR_ID)));
        assertThat(response.getRoleAssignmentResponse().get(0).getRoleName(), is(equalTo(ROLE_NAME)));
    }

    @Test
    @PactTestFor(pactMethod = "queryRoleAssignmentsWithLabels")
    public void verifyQueryRoleAssignmentsWithLabels() {
        RoleAssignmentServiceResponse response = roleAssignmentsService.getRoleAssignmentsWithLabels(
            ACTOR_ID,
            AUTHORIZATION_TOKEN,
            List.of(ROLE_NAME)
        );

        assertThat(response.getRoleAssignmentResponse().size(), is(equalTo(1)));
        assertThat(response.getRoleAssignmentResponse().get(0).getRoleLabel(), is(equalTo(ROLE_LABEL)));
    }

    @Test
    @PactTestFor(pactMethod = "queryRoleAssignmentsByCaseIdAndRole")
    public void verifyQueryRoleAssignmentsByCaseIdAndRole() {
        RoleAssignmentServiceResponse response = roleAssignmentsService.queryRoleAssignmentsByCaseIdAndRole(
            CASE_ID,
            List.of(ROLE_TYPE),
            List.of(ROLE_NAME),
            AUTHORIZATION_TOKEN
        );

        assertThat(response.getRoleAssignmentResponse().size(), is(equalTo(1)));
        assertThat(response.getRoleAssignmentResponse().get(0).getAttributes().getCaseId(), is(equalTo(CASE_ID)));
    }

    @Test
    @PactTestFor(pactMethod = "createRoleAssignment")
    public void verifyCreateRoleAssignment() {
        roleAssignmentsService.assignUserRoles(ACTOR_ID, AUTHORIZATION_TOKEN, buildRoleAssignmentRequest());
    }

    private QueryRequest buildActorRoleQueryRequest() {
        return new QueryRequest()
            .setActorId(List.of(ACTOR_ID))
            .setRoleName(List.of(ROLE_NAME));
    }

    private QueryRequest buildCaseRoleQueryRequest() {
        return new QueryRequest()
            .setRoleType(List.of(ROLE_TYPE))
            .setRoleName(List.of(ROLE_NAME))
            .setAttributes(Map.of("caseId", List.of(CASE_ID)));
    }

    private String buildCreateRoleAssignmentRequest() {
        return """
            {
              "requestedRoles": [
                {
                  "actorIdType": "IDAM",
                  "attributes": {
                    "caseType": "CIVIL",
                    "jurisdiction": "CIVIL"
                  },
                  "status": "CREATE_REQUESTED",
                  "classification": "%s",
                  "actorId": "%s",
                  "grantType": "%s",
                  "roleCategory": "%s",
                  "roleName": "%s",
                  "roleType": "%s",
                  "readOnly": false
                }
              ],
              "roleRequest": {
                "assignerId": "%s",
                "process": "%s",
                "reference": "%s",
                "replaceExisting": true
              }
            }
            """.formatted(
            CLASSIFICATION,
            ACTOR_ID,
            GRANT_TYPE,
            ROLE_CATEGORY,
            ROLE_NAME,
            ROLE_TYPE,
            ASSIGNER_ID,
            ROLE_PROCESS,
            ROLE_REFERENCE
        );
    }

    private RoleAssignmentRequest buildRoleAssignmentRequest() {
        RoleRequest roleRequest = new RoleRequest()
            .setAssignerId(ASSIGNER_ID)
            .setReference(ROLE_REFERENCE)
            .setProcess(ROLE_PROCESS)
            .setReplaceExisting(true);

        RoleAssignment roleAssignment = new RoleAssignment()
            .setActorId(ACTOR_ID)
            .setActorIdType("IDAM")
            .setRoleType(RoleType.ORGANISATION)
            .setClassification(CLASSIFICATION)
            .setGrantType(GrantType.STANDARD)
            .setRoleCategory(RoleCategory.SYSTEM)
            .setRoleName(ROLE_NAME)
            .setAttributes(Map.of("jurisdiction", "CIVIL", "caseType", "CIVIL"))
            .setReadOnly(false);

        return new RoleAssignmentRequest()
            .setRoleRequest(roleRequest)
            .setRequestedRoles(List.of(roleAssignment));
    }

    private DslPart buildRoleAssignmentResponse() {
        return LambdaDsl.newJsonBody(root -> root
            .minArrayLike("roleAssignmentResponse", 1, 1, roleAssignment -> roleAssignment
                .stringValue("actorId", ACTOR_ID)
                .stringValue("actorIdType", "IDAM")
                .stringValue("roleType", ROLE_TYPE)
                .stringValue("roleName", ROLE_NAME)
                .stringValue("roleLabel", ROLE_LABEL)
                .stringValue("classification", CLASSIFICATION)
                .stringValue("grantType", GRANT_TYPE)
                .stringValue("roleCategory", ROLE_CATEGORY)
                .booleanValue("readOnly", false)
                .object("attributes", attributes -> attributes
                    .stringValue("caseId", CASE_ID)
                    .stringValue("jurisdiction", "CIVIL")
                    .stringValue("caseType", "CIVIL")))
        ).build();
    }

    private DslPart buildCreateRoleAssignmentResponse() {
        return LambdaDsl.newJsonBody(root -> root.object("roleAssignmentResponse", roleAssignmentResponse -> {
            roleAssignmentResponse.minArrayLike("requestedRoles", 1, 1, roleAssignment -> roleAssignment
                .stringValue("actorId", ACTOR_ID)
                .stringValue("actorIdType", "IDAM")
                .stringValue("roleType", ROLE_TYPE)
                .stringValue("roleName", ROLE_NAME)
                .stringValue("classification", CLASSIFICATION)
                .stringValue("grantType", GRANT_TYPE)
                .stringValue("roleCategory", ROLE_CATEGORY)
                .booleanValue("readOnly", false)
                .object("attributes", attributes -> attributes
                    .stringValue("jurisdiction", "CIVIL")
                    .stringValue("caseType", "CIVIL")));
            roleAssignmentResponse.object("roleRequest", roleRequest -> roleRequest
                .stringValue("assignerId", ASSIGNER_ID)
                .stringValue("process", ROLE_PROCESS)
                .stringValue("reference", ROLE_REFERENCE)
                .booleanValue("replaceExisting", true));
        })).build();
    }
}
