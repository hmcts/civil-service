package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class DetermineLoggedInSolicitorTest {

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

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) determineLoggedInSolicitor.execute(callbackParams);

        assertThat(response.getData().get("isRespondent1")).isEqualTo("Yes");
        assertThat(response.getData().get("isRespondent2")).isEqualTo("No");
        assertThat(response.getData().get("isApplicant1")).isEqualTo("No");
    }

    @Test
    void shouldSetRespondent2RoleWhenSolicitorHasRespondent2Role() {
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORONE)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORTWO)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) determineLoggedInSolicitor.execute(callbackParams);

        assertThat(response.getData().get("isRespondent1")).isEqualTo("No");
        assertThat(response.getData().get("isRespondent2")).isEqualTo("Yes");
        assertThat(response.getData().get("isApplicant1")).isEqualTo("No");
    }

    @Test
    void shouldSetApplicant1RoleWhenSolicitorHasApplicant1Role() {
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORONE)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORTWO)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", APPLICANTSOLICITORONE)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) determineLoggedInSolicitor.execute(callbackParams);

        assertThat(response.getData().get("isRespondent1")).isEqualTo("No");
        assertThat(response.getData().get("isRespondent2")).isEqualTo("No");
        assertThat(response.getData().get("isApplicant1")).isEqualTo("Yes");
    }

    @Test
    void shouldSetNoRolesWhenSolicitorHasNoRoles() {
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORONE)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", RESPONDENTSOLICITORTWO)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole("1234", "userId", APPLICANTSOLICITORONE)).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) determineLoggedInSolicitor.execute(callbackParams);

        assertThat(response.getData().get("isRespondent1")).isNull();
        assertThat(response.getData().get("isRespondent2")).isNull();
        assertThat(response.getData().get("isApplicant1")).isNull();
    }

    @Test
    void shouldSetNeitherCompanyNorOrganisationToNoWhenPartyIsCompany() {
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .respondent2DetailsForClaimDetailsTab(Party.builder().type(Party.Type.COMPANY).build())
            .ccdCaseReference(1234L)
            .build();

        callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) determineLoggedInSolicitor.execute(callbackParams);

        assertThat(response.getData().get("neitherCompanyNorOrganisation")).isEqualTo("No");
    }

    @Test
    void shouldSetNeitherCompanyNorOrganisationToNoWhenPartyIsOrganisation() {
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .respondent2DetailsForClaimDetailsTab(Party.builder().type(Party.Type.ORGANISATION).build())
            .ccdCaseReference(1234L)
            .build();

        callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) determineLoggedInSolicitor.execute(callbackParams);

        assertThat(response.getData().get("neitherCompanyNorOrganisation")).isEqualTo("No");
    }

    @Test
    void shouldSetNeitherCompanyNorOrganisationToYesWhenPartyIsNeitherCompanyNorOrganisation() {
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .respondent2DetailsForClaimDetailsTab(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .ccdCaseReference(1234L)
            .build();

        callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) determineLoggedInSolicitor.execute(callbackParams);

        assertThat(response.getData().get("neitherCompanyNorOrganisation")).isEqualTo("Yes");
    }
}
