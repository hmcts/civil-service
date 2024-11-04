package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

@ExtendWith(MockitoExtension.class)
class DetermineLoggedInSolicitorTest {

    @Mock
    private UserService userService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    private DetermineLoggedInSolicitor determineLoggedInSolicitor;

    private CallbackParams callbackParams;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        determineLoggedInSolicitor = new DetermineLoggedInSolicitor(userService, coreCaseUserService, objectMapper);
        CaseData caseData = CaseData.builder().ccdCaseReference(1234L).build();
        UserInfo userInfo = UserInfo.builder().uid("userId").build();
        callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
        when(userService.getUserInfo(any())).thenReturn(userInfo);
    }

    @Test
    void shouldSetRespondent1RoleWhenSolicitorHasRespondent1Role() {
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORONE)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = executeCallback();

        assertRoles(response, "Yes", "No", "No");
    }

    @Test
    void shouldSetRespondent2RoleWhenSolicitorHasRespondent2Role() {
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORONE)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORTWO)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = executeCallback();

        assertRoles(response, "No", "Yes", "No");
    }

    @Test
    void shouldSetApplicant1RoleWhenSolicitorHasApplicant1Role() {
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORONE)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORTWO)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", APPLICANTSOLICITORONE)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = executeCallback();

        assertRoles(response, "No", "No", "Yes");
    }

    @Test
    void shouldSetNoRolesWhenSolicitorHasNoRoles() {
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORONE)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORTWO)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", APPLICANTSOLICITORONE)).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse response = executeCallback();

        assertRoles(response, null, null, null);
    }

    @Test
    void shouldSetNeitherCompanyNorOrganisationToNoWhenPartyIsCompany() {
        CaseData caseData = buildCaseData(Party.Type.COMPANY);

        callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback();

        assertThat(response.getData()).containsEntry("neitherCompanyNorOrganisation", "No");
    }

    @Test
    void shouldSetNeitherCompanyNorOrganisationToNoWhenPartyIsOrganisation() {
        CaseData caseData = buildCaseData(Party.Type.ORGANISATION);

        callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback();

        assertThat(response.getData()).containsEntry("neitherCompanyNorOrganisation", "No");
    }

    @Test
    void shouldSetNeitherCompanyNorOrganisationToYesWhenPartyIsNeitherCompanyNorOrganisation() {
        CaseData caseData = buildCaseData(Party.Type.INDIVIDUAL);

        callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback();

        assertThat(response.getData()).containsEntry("neitherCompanyNorOrganisation", "Yes");
    }

    private AboutToStartOrSubmitCallbackResponse executeCallback() {
        return (AboutToStartOrSubmitCallbackResponse) determineLoggedInSolicitor.execute(callbackParams);
    }

    private void assertRoles(AboutToStartOrSubmitCallbackResponse response, String isRespondent1, String isRespondent2, String isApplicant1) {
        assertThat(response.getData()).containsEntry("isRespondent1", isRespondent1);
        assertThat(response.getData()).containsEntry("isRespondent2", isRespondent2);
        assertThat(response.getData()).containsEntry("isApplicant1", isApplicant1);
    }

    private CaseData buildCaseData(Party.Type partyType) {
        return CaseData.builder()
            .isRespondent2(YesOrNo.YES)
            .respondent2DetailsForClaimDetailsTab(Party.builder().type(partyType).build())
            .ccdCaseReference(1234L)
            .build();
    }

    private CallbackParams buildCallbackParams(CaseData caseData) {
        return CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();
    }
}
