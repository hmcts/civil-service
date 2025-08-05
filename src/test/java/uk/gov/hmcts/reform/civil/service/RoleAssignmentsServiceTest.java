package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.ras.client.RoleAssignmentsApi;
import uk.gov.hmcts.reform.civil.ras.model.GrantType;
import uk.gov.hmcts.reform.civil.ras.model.QueryRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignment;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleCategory;
import uk.gov.hmcts.reform.civil.ras.model.RoleRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.ras.model.RoleType.ORGANISATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RoleAssignmentsService.class
})
class RoleAssignmentsServiceTest {

    private static final String USER_AUTH_TOKEN = "Bearer caa-user-xyz";
    private static final String SERVICE_TOKEN = "Bearer service-token";
    private static final String ACTOR_ID = "1111111";
    private static final String CASE_ID = "123456789";
    private static final List<String> ROLE_TYPE = List.of("test_role_type_case");
    private static final List<String> ROLE_NAME = List.of("test_role_name_judge", "test_role_name_judge_lead");
    private static final RoleAssignmentServiceResponse RAS_RESPONSE = RoleAssignmentServiceResponse
        .builder()
        .roleAssignmentResponse(
            List.of(RoleAssignmentResponse
                        .builder()
                        .actorId(ACTOR_ID)
                        .build()
            )
        )
        .build();

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private RoleAssignmentsApi roleAssignmentApi;

    @Autowired
    private RoleAssignmentsService roleAssignmentsService;

    @BeforeEach
    void init() {
        clearInvocations(authTokenGenerator);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(roleAssignmentApi.getRoleAssignments(anyString(), anyString(), anyString())).thenReturn(RAS_RESPONSE);
    }

    @Test
    void getRoleAssignmentsWithLabels_shouldReturnExpectAssignments() {
        RoleAssignmentServiceResponse expected = RoleAssignmentServiceResponse.builder()
            .roleAssignmentResponse(
                List.of(RoleAssignmentResponse
                            .builder()
                            .actorId(ACTOR_ID)
                            .roleLabel("Role Label")
                            .build()
                )
            )
            .build();
        when(roleAssignmentApi.getRoleAssignments(
                 USER_AUTH_TOKEN,
                 SERVICE_TOKEN,
                 null,
                 null,
                 100,
                 null,
                 null,
                 QueryRequest.builder().actorId(ACTOR_ID).roleName(ROLE_NAME).build(),
                 true
             )
        ).thenReturn(expected);

        var actual = roleAssignmentsService.getRoleAssignmentsWithLabels(ACTOR_ID, USER_AUTH_TOKEN, ROLE_NAME);

        assertEquals(expected, actual);
    }

    @Test
    void getRoleAssignmentsByCaseIdAndRole_shouldReturnExpectAssignments() {
        RoleAssignmentServiceResponse expected = RoleAssignmentServiceResponse.builder()
            .roleAssignmentResponse(
                List.of(RoleAssignmentResponse
                            .builder()
                            .actorId(ACTOR_ID)
                            .roleLabel("Role Label")
                            .build()
                )
            )
            .build();

        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(ROLE_TYPE)
            .roleName(ROLE_NAME)
            .attributes(Map.of("caseId", List.of(CASE_ID)))
            .build();

        when(roleAssignmentApi.getRoleAssignments(
                 USER_AUTH_TOKEN,
                 SERVICE_TOKEN,
                 null,
                 null,
                 null,
                 "roleName",
                 null,
                 queryRequest,
                 true
             )
        ).thenReturn(expected);

        var actual = roleAssignmentsService.queryRoleAssignmentsByCaseIdAndRole(
            CASE_ID,
            ROLE_TYPE,
            ROLE_NAME,
            USER_AUTH_TOKEN
        );

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturn() {
        var roleAssignmentsExpected = roleAssignmentsService.getRoleAssignments(ACTOR_ID, USER_AUTH_TOKEN);
        assertEquals(RAS_RESPONSE, roleAssignmentsExpected);
    }

    @Test
    void shouldPostExpectedPayload() {
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .roleRequest(
                RoleRequest.builder()
                    .assignerId(ACTOR_ID)
                    .reference("civil-hearings-system-user")
                    .process("civil-system-user")
                    .replaceExisting(true)
                    .build())
            .requestedRoles(List.of(
                RoleAssignment.builder()
                    .actorId(ACTOR_ID)
                    .actorIdType("IDAM")
                    .roleType(ORGANISATION)
                    .classification("PUBLIC")
                    .grantType(GrantType.STANDARD)
                    .roleCategory(RoleCategory.SYSTEM)
                    .roleName("some-role")
                    .attributes(Map.of("jurisdiction", "CIVIL", "caseType", "CIVIL"))
                    .readOnly(false)
                    .build()
            )).build();

        roleAssignmentsService.assignUserRoles(ACTOR_ID, USER_AUTH_TOKEN, request);

        verify(roleAssignmentApi, times(1))
            .createRoleAssignment(USER_AUTH_TOKEN, SERVICE_TOKEN, request);
    }

}
