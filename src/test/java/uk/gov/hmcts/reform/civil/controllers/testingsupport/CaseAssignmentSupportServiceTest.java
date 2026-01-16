package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentSupportServiceTest {

    private static final String CAA_TOKEN = "Bearer test-token";
    private static final String SERVICE_TOKEN = "service-token";

    @Mock
    private CaseAssignmentApi caseAssignmentApi;
    @Mock
    private UserService userService;
    @Mock
    private CrossAccessUserConfiguration crossAccessUserConfiguration;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamClient idamClient;

    private CaseAssignmentSupportService caseAssignmentSupportService;

    @BeforeEach
    void setUp() {
        caseAssignmentSupportService = new CaseAssignmentSupportService(
            caseAssignmentApi,
            userService,
            crossAccessUserConfiguration,
            authTokenGenerator,
            idamClient
        );
        lenient().when(crossAccessUserConfiguration.getUserName()).thenReturn("caa-user");
        lenient().when(crossAccessUserConfiguration.getPassword()).thenReturn("caa-pass");
        lenient().when(userService.getAccessToken("caa-user", "caa-pass")).thenReturn(CAA_TOKEN);
        lenient().when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    void shouldUnassignUserFromCasesUsingEmail() {
        List<String> caseIds = List.of("1111111111111111");
        String userEmail = "tester@example.com";
        UserDetails idamUser = UserDetails.builder().id("user-123").email(userEmail).build();
        when(idamClient.searchUsers(CAA_TOKEN, "email:" + userEmail)).thenReturn(List.of(idamUser));

        CaseAssignmentUserRole role = CaseAssignmentUserRole.builder()
            .caseDataId(caseIds.get(0))
            .userId("user-123")
            .caseRole("[APPLICANT]")
            .build();
        CaseAssignmentUserRolesResource rolesResource = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(role))
            .build();
        when(caseAssignmentApi.getUserRoles(CAA_TOKEN, SERVICE_TOKEN, caseIds)).thenReturn(rolesResource);

        caseAssignmentSupportService.unAssignUserFromCasesByEmail(caseIds, "ORG1", userEmail);

        ArgumentCaptor<CaseAssignmentUserRolesRequest> captor = ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);
        verify(caseAssignmentApi).removeCaseUserRoles(eq(CAA_TOKEN), eq(SERVICE_TOKEN), captor.capture());
        CaseAssignmentUserRolesRequest request = captor.getValue();
        List<CaseAssignmentUserRoleWithOrganisation> assignments = request.getCaseAssignmentUserRolesWithOrganisation();
        assertThat(assignments).hasSize(1);
        CaseAssignmentUserRoleWithOrganisation assignment = assignments.get(0);
        assertThat(assignment.getCaseDataId()).isEqualTo(caseIds.get(0));
        assertThat(assignment.getUserId()).isEqualTo("user-123");
        assertThat(assignment.getOrganisationId()).isEqualTo("ORG1");
    }

    @Test
    void shouldThrowWhenEmailMissing() {
        assertThatThrownBy(() -> caseAssignmentSupportService.unAssignUserFromCasesByEmail(List.of("123"), "ORG1", null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
