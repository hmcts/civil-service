package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.ga.service.StateGeneratorService;
import uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_JUDGE_BUSINESS_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    EndJudgeMakesDecisionBusinessProcessCallbackHandler.class,
    CoreCaseDataService.class,
    ObjectMapper.class,
    JudicialDecisionNotificationUtil.class
})
class EndJudgeMakesDecisionBusinessProcessCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    private EndJudgeMakesDecisionBusinessProcessCallbackHandler handler;
    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private CoreCaseDataService coreCaseDataService;

    @MockitoBean
    private StateGeneratorService stateGeneratorService;
    @MockitoBean
    private ParentCaseUpdateHelper parentCaseUpdateHelper;
    @MockitoBean
    private JudicialDecisionHelper judicialDecisionHelper;
    private CallbackParams params;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(END_JUDGE_BUSINESS_PROCESS_GASPEC);
    }

    @Test
    void shouldAddRespondentSolicitorDetail_WhenJudeOrderMakeUncloakApplication() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(NO).build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        when(judicialDecisionHelper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).thenReturn(true);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any())).thenReturn(CaseState.ORDER_MADE);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1))
            .updateParentApplicationVisibilityWithNewState(any(), any());
    }

    @Test
    void shouldAddRespondentSolicitorDetail_WhenJudeOrderMakeUncloakApplication_WhenLR() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(NO).build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        when(judicialDecisionHelper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).thenReturn(true);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any())).thenReturn(CaseState.ORDER_MADE);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1))
            .updateParentApplicationVisibilityWithNewState(any(), any());

    }

    @Test
    void shouldNotAddRespondentSolicitorDetail_WhenJudeOrderMake_WithNoticeApplication() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YES).build();
        caseData = caseData.toBuilder().isGaRespondentOneLip(YES).build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        when(judicialDecisionHelper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).thenReturn(false);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any())).thenReturn(CaseState.ORDER_MADE);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1))
            .updateParentWithGAState(any(), any());
    }

    @Test
    void shouldNotAddRespondentSolicitorDetail_WhenJudeOrderMake_WithNoticeApplicationLipNotAdditionalPayment() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(NO).build();
        caseData = caseData.toBuilder().isGaRespondentOneLip(YES).build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        when(judicialDecisionHelper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).thenReturn(true);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any())).thenReturn(CaseState.ORDER_MADE);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1))
            .updateParentApplicationVisibilityWithNewState(any(), any());
    }

}
