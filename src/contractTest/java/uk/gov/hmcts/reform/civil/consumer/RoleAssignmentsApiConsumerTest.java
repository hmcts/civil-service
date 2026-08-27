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
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@MockServerConfig(hostInterface = "localhost", port = "6681")
@TestPropertySource(properties = "role-assignment-service.api.url=http://localhost:6681")
public class RoleAssignmentsApiConsumerTest extends BaseContractTest {

    private static final String ROLE_ASSIGNMENT_CREATE_PROVIDER = "am_roleAssignment_createAssignment";
    private static final String ROLE_ASSIGNMENT_GET_PROVIDER = "am_roleAssignment_getAssignment";
    private static final String ROLE_ASSIGNMENT_QUERY_PROVIDER = "am_roleAssignment_queryAssignment";
    private static final String ROLE_ASSIGNMENT_GET_MEDIA_TYPE =
        "application/vnd.uk.gov.hmcts.role-assignment-service.get-assignments+json;charset=UTF-8;version=1.0";
    private static final String ROLE_ASSIGNMENT_QUERY_MEDIA_TYPE =
        "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;charset=UTF-8;version=1.0";
    private static final String ROLE_ASSIGNMENT_CREATE_MEDIA_TYPE =
        "application/vnd.uk.gov.hmcts.role-assignment-service.create-assignments+json;charset=UTF-8;version=1.0";
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

    @Pact(consumer = "civil_service", provider = ROLE_ASSIGNMENT_GET_PROVIDER)
    public RequestResponsePact getRoleAssignmentsForActor(PactDslWithProvider builder) {
        return builder
            .given("An actor with provided id is available in role assignment service")
            .uponReceiving("a get role assignments by actor id request")
            .path("/am/role-assignments/actors/" + ACTOR_ID)
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, roleAssignmentMediaTypeMatcher("get-assignments"),
                         ROLE_ASSIGNMENT_GET_MEDIA_TYPE)
            .body(buildRoleAssignmentResponse(false))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service", provider = ROLE_ASSIGNMENT_QUERY_PROVIDER)
    public RequestResponsePact queryRoleAssignmentsWithLabels(PactDslWithProvider builder)
        throws JSONException, IOException {
        return builder
            .given("A list of role assignments for the search query")
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
            .matchHeader(HttpHeaders.CONTENT_TYPE, roleAssignmentMediaTypeMatcher("post-assignment-query-request"),
                         ROLE_ASSIGNMENT_QUERY_MEDIA_TYPE)
            .body(buildRoleAssignmentResponse(true))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service", provider = ROLE_ASSIGNMENT_QUERY_PROVIDER)
    public RequestResponsePact queryRoleAssignmentsByCaseIdAndRole(PactDslWithProvider builder)
        throws JSONException, IOException {
        return builder
            .given("A list of role assignments for the search query by attributes")
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
            .matchHeader(HttpHeaders.CONTENT_TYPE, roleAssignmentMediaTypeMatcher("post-assignment-query-request"),
                         ROLE_ASSIGNMENT_QUERY_MEDIA_TYPE)
            .body(buildRoleAssignmentResponse(false))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service", provider = ROLE_ASSIGNMENT_CREATE_PROVIDER)
    public RequestResponsePact createRoleAssignment(PactDslWithProvider builder) {
        return builder
            .given("The assignment request is valid with one requested role and replaceExisting flag as true")
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
            .matchHeader(HttpHeaders.CONTENT_TYPE, roleAssignmentMediaTypeMatcher("create-assignments"),
                         ROLE_ASSIGNMENT_CREATE_MEDIA_TYPE)
            .body(buildCreateRoleAssignmentResponse())
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    private String roleAssignmentMediaTypeMatcher(String operation) {
        return "application/(json|vnd\\.uk\\.gov\\.hmcts\\.role-assignment-service\\."
            + operation
            + "\\+json;charset=UTF-8;version=1\\.0)(;.*)?";
    }

    @Test
    @PactTestFor(providerName = ROLE_ASSIGNMENT_GET_PROVIDER, pactMethod = "getRoleAssignmentsForActor")
    public void verifyGetRoleAssignmentsForActor() {
        RoleAssignmentServiceResponse response = roleAssignmentsService.getRoleAssignments(ACTOR_ID, AUTHORIZATION_TOKEN);

        assertThat(response.getRoleAssignmentResponse().size(), is(equalTo(1)));
        assertThat(response.getRoleAssignmentResponse().get(0).getActorId(), is(equalTo(ACTOR_ID)));
        assertThat(response.getRoleAssignmentResponse().get(0).getRoleName(), is(equalTo(ROLE_NAME)));
    }

    @Test
    @PactTestFor(providerName = ROLE_ASSIGNMENT_QUERY_PROVIDER, pactMethod = "queryRoleAssignmentsWithLabels")
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
    @PactTestFor(providerName = ROLE_ASSIGNMENT_QUERY_PROVIDER, pactMethod = "queryRoleAssignmentsByCaseIdAndRole")
    public void verifyQueryRoleAssignmentsByCaseIdAndRole() {
        RoleAssignmentServiceResponse response = roleAssignmentsService.queryRoleAssignmentsByCaseIdAndRole(
            CASE_ID,
            List.of(ROLE_TYPE),
            List.of(ROLE_NAME),
            AUTHORIZATION_TOKEN
        );

        assertThat(response.getRoleAssignmentResponse().size(), is(equalTo(1)));
        assertThat(response.getRoleAssignmentResponse().get(0).getAttributes(), is(notNullValue()));
    }

    @Test
    @PactTestFor(providerName = ROLE_ASSIGNMENT_CREATE_PROVIDER, pactMethod = "createRoleAssignment")
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

    private DslPart buildRoleAssignmentResponse(boolean includeRoleLabel) {
        return LambdaDsl.newJsonBody(root -> root
            .minArrayLike("roleAssignmentResponse", 1, 1, roleAssignment -> {
                roleAssignment
                    .stringType("actorId", ACTOR_ID)
                    .stringType("actorIdType", "IDAM")
                    .stringType("roleType", ROLE_TYPE)
                    .stringType("roleName", ROLE_NAME)
                    .stringType("classification", CLASSIFICATION)
                    .stringType("grantType", GRANT_TYPE)
                    .stringType("roleCategory", ROLE_CATEGORY)
                    .booleanValue("readOnly", false)
                    .object("attributes", attributes -> attributes
                        .stringType("jurisdiction", "CIVIL"));
                if (includeRoleLabel) {
                    roleAssignment.stringType("roleLabel", ROLE_LABEL);
                }
            })
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
