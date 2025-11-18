package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.ras.client.RoleAssignmentsApi;
import uk.gov.hmcts.reform.civil.ras.model.QueryRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentsServiceUnitTest {

    private static final String AUTH = "Bearer token";
    private static final String ACTOR_ID = "actor";
    private static final String CASE_ID = "12345";

    @Mock
    private RoleAssignmentsApi roleAssignmentsApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @InjectMocks
    private RoleAssignmentsService service;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn("service-token");
    }

    @Test
    void shouldDelegateGetRoleAssignments() {
        RoleAssignmentServiceResponse response = RoleAssignmentServiceResponse.builder().build();
        when(roleAssignmentsApi.getRoleAssignments(AUTH, "service-token", ACTOR_ID)).thenReturn(response);

        assertThat(service.getRoleAssignments(ACTOR_ID, AUTH)).isEqualTo(response);
    }

    @Test
    void shouldDelegateQueryByCaseIdAndRole() {
        RoleAssignmentServiceResponse response = RoleAssignmentServiceResponse.builder().build();
        List<String> roles = List.of("judge");
        when(roleAssignmentsApi.getRoleAssignments(
            eq(AUTH),
            eq("service-token"),
            eq(null),
            eq(null),
            eq(null),
            eq("roleName"),
            eq(null),
            eq(QueryRequest.builder()
                .roleType(roles)
                .roleName(roles)
                .attributes(Map.of("caseId", List.of(CASE_ID)))
                .build()),
            eq(true)
        )).thenReturn(response);

        assertThat(service.queryRoleAssignmentsByCaseIdAndRole(CASE_ID, roles, roles, AUTH)).isEqualTo(response);
    }

    @Test
    void shouldDelegateAssignmentCreation() {
        RoleAssignmentRequest request = RoleAssignmentRequest.builder().build();

        service.assignUserRoles(ACTOR_ID, AUTH, request);

        verify(roleAssignmentsApi).createRoleAssignment(AUTH, "service-token", request);
    }
}
