package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RespondToClaimSpecCallbackHandler.class,
        ExitSurveyConfiguration.class,
        ExitSurveyContentService.class,
        JacksonAutoConfiguration.class,
        ValidationAutoConfiguration.class,
        DateOfBirthValidator.class,
        UnavailableDateValidator.class,
        CaseDetailsConverter.class,
        LocationReferenceDataService.class,
        CourtLocationUtils.class,
        SimpleStateFlowEngine.class,
        SimpleStateFlowBuilder.class,
        AssignCategoryId.class,
        FrcDocumentsUtils.class,
        RespondToClaimSpecCallbackHandlerTestConfig.class
})
class RespondToClaimSpecCallbackHandlerMidDetermineLoggedInSolicitorTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;

    @MockBean
    private PaymentDateValidator validator;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private SimpleStateFlowBuilder simpleStateFlowBuilder;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @MockBean
    private Time time;

    @BeforeEach
    void setup() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
    }

    private AboutToStartOrSubmitCallbackResponse handleCallback(CaseData caseData) {
        CallbackParams params = callbackParamsOf(caseData, MID, "determineLoggedInSolicitor");
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
    }

    private void assertCommonResponse(AboutToStartOrSubmitCallbackResponse response) {
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()).isNotNull();
    }

    @Test
    public void testDetermineLoggedInSolicitorForRespondentSolicitor1() {
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
        assertCommonResponse(response);
        assertThat(response.getData()).containsEntry("isRespondent1", "Yes");
        assertThat(response.getData()).containsEntry("isRespondent2", "No");
        assertThat(response.getData()).containsEntry("isApplicant1", "No");
        assertThat(response.getData()).containsEntry("neitherCompanyNorOrganisation", "Yes");
    }

    @Test
    public void testDetermineLoggedInSolicitorForRespondentSolicitor2() {
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().isRespondent2(YES).build();
        AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
        assertCommonResponse(response);
        assertThat(response.getData()).containsEntry("isRespondent1", "No");
        assertThat(response.getData()).containsEntry("isRespondent2", "Yes");
        assertThat(response.getData()).containsEntry("isApplicant1", "No");
        assertThat(response.getData()).containsEntry("neitherCompanyNorOrganisation", "Yes");
    }

    @Test
    public void testDetermineLoggedInSolicitorForApplicantSolicitor() {
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(APPLICANTSOLICITORONE))).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
        assertCommonResponse(response);
        assertThat(response.getData()).containsEntry("isRespondent1", "No");
        assertThat(response.getData()).containsEntry("isRespondent2", "No");
        assertThat(response.getData()).containsEntry("isApplicant1", "Yes");
    }
}
