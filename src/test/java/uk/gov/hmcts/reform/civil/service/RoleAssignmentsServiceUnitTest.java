package uk.gov.hmcts.reform.civil.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
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
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn("service-token");
        Logger logger = (Logger) LoggerFactory.getLogger(RoleAssignmentsService.class);
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(RoleAssignmentsService.class)).setLevel(originalLevel);
    }

    @Test
    void shouldDelegateGetRoleAssignments() {
        RoleAssignmentServiceResponse response = new RoleAssignmentServiceResponse();
        when(roleAssignmentsApi.getRoleAssignments(AUTH, "service-token", ACTOR_ID)).thenReturn(response);

        assertThat(service.getRoleAssignments(ACTOR_ID, AUTH)).isEqualTo(response);
    }

    @Test
    void shouldDelegateQueryByCaseIdAndRole() {
        RoleAssignmentServiceResponse response = new RoleAssignmentServiceResponse();
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

    @Test
    void shouldGetRoleAssignmentsWithLabels() {
        RoleAssignmentServiceResponse response = new RoleAssignmentServiceResponse();
        List<String> roleNames = List.of("judge");
        when(roleAssignmentsApi.getRoleAssignments(
            eq(AUTH),
            eq("service-token"),
            eq(null),
            eq(null),
            eq(100),
            eq(null),
            eq(null),
            eq(QueryRequest.builder().actorId(ACTOR_ID).roleName(roleNames).build()),
            eq(true)
        )).thenReturn(response);

        assertThat(service.getRoleAssignmentsWithLabels(ACTOR_ID, AUTH, roleNames)).isEqualTo(response);
    }
}
