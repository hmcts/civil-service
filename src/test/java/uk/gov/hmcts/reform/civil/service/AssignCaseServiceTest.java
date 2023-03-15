package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;

@ExtendWith(SpringExtension.class)
public class AssignCaseServiceTest {

    private static final String AUTHORIZATION = "authorisation";
    private static final String CASE_ID = "1";
    private static final String UID = "abra-abra-cadabra";
    private static final String ORG_ID = "org_id";
    private static final UserInfo USER_INFO = UserInfo.builder().uid(UID).build();
    private static final Organisation ORGANISATION = Organisation
        .builder()
        .organisationIdentifier(ORG_ID)
        .build();

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private UserService userService;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private AssignCaseService assignCaseService;

    @BeforeEach
    void setUp() {
        given(userService.getUserInfo(anyString())).willReturn(USER_INFO);
    }

    @Test
    void shouldCallAssignCaseWithOrganisationIdSuccessfully() {
        given(organisationService.findOrganisation(anyString())).willReturn(Optional.of(ORGANISATION));

        assignCaseService.assignCase(AUTHORIZATION, CASE_ID, Optional.of(RESPONDENTSOLICITORONE));

        verify(coreCaseUserService, times(1))
            .assignCase(CASE_ID, UID, ORG_ID, RESPONDENTSOLICITORONE);
    }

    @Test
    void shouldCallAssignCaseWithoutOrganisationIdSuccessfully() {
        assignCaseService.assignCase(AUTHORIZATION, CASE_ID, Optional.of(RESPONDENTSOLICITORONE));
        verify(coreCaseUserService, times(1))
            .assignCase(CASE_ID, UID, null, RESPONDENTSOLICITORONE);
    }

}
