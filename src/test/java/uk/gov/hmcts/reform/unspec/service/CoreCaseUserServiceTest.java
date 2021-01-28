package uk.gov.hmcts.reform.unspec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.unspec.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.unspec.enums.CaseRole;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    CoreCaseUserService.class
})
class CoreCaseUserServiceTest {

    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";
    private static final String DEFENDANT_USER_AUTH_TOKEN = "Bearer defendant-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final String CASE_ID = "1";
    private static final String USER_ID = "User1";
    public static final String ORG_ID = "62LYJRF";

    @MockBean
    private CrossAccessUserConfiguration userConfig;

    @MockBean
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private Organisation organisation;

    @Autowired
    private CoreCaseUserService service;

    @BeforeEach
    void init() {
        clearInvocations(authTokenGenerator);
        clearInvocations(idamClient);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);
        when(idamClient.getUserInfo(DEFENDANT_USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
        when(organisationService.findOrganisation(DEFENDANT_USER_AUTH_TOKEN)).thenReturn(Optional.of(organisation));
        when(organisation.getOrganisationIdentifier()).thenReturn(ORG_ID);
    }

    @Nested
    class AssignCase {

        @Test
        void shouldAssignCaseToUser_WhenCalled() {
            service.assignCase(CASE_ID, DEFENDANT_USER_AUTH_TOKEN);

            verify(organisationService).findOrganisation(DEFENDANT_USER_AUTH_TOKEN);

            CaseAssignedUserRoleWithOrganisation caseAssignedUserRoleWithOrganisation
                = CaseAssignedUserRoleWithOrganisation.builder()
                .caseDataId(CASE_ID)
                .userId(USER_ID)
                .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                .organisationId(ORG_ID)
                .build();

            AddCaseAssignedUserRolesRequest addCaseAssignedUserRolesRequest = AddCaseAssignedUserRolesRequest.builder()
                .caseAssignedUserRoles(List.of(caseAssignedUserRoleWithOrganisation))
                .build();

            verify(caseAccessDataStoreApi).addCaseUserRoles(
                USER_AUTH_TOKEN,
                SERVICE_AUTH_TOKEN,
                addCaseAssignedUserRolesRequest
            );
        }

    }
}
