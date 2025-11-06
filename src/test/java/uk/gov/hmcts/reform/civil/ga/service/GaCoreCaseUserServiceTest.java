package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GaCoreCaseUserService.class
})
class GaCoreCaseUserServiceTest {

    private static final String CAA_USER_AUTH_TOKEN = "Bearer caa-user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final String CASE_ID = "1";
    private static final String USER_ID = "User1";
    private static final String USER_ID2 = "User2";
    @MockBean
    private CrossAccessUserConfiguration userConfig;

    @MockBean
    private CaseAssignmentApi caseAccessDataStoreApi;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private GaCoreCaseUserService service;

    @BeforeEach
    void init() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(
            CAA_USER_AUTH_TOKEN);
        CaseAssignmentUserRolesResource caseAssignedUserRolesResource = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder()
                    .userId(USER_ID)
                    .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                    .build(),
                CaseAssignmentUserRole.builder()
                    .userId(USER_ID2)
                    .caseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                    .build()
            ))
            .build();
        when(caseAccessDataStoreApi.getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID)))
            .thenReturn(caseAssignedUserRolesResource);
    }

    @Test
    void shouldReturnUserRoles_getUserRoles() {
        assertThat(service.getUserRoles(CASE_ID).getCaseAssignmentUserRoles()).hasSize(2);
    }
}
