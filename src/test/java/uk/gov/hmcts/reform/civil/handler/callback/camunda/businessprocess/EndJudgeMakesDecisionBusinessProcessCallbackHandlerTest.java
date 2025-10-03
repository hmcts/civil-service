package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.service.StateGeneratorService;
import uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil;

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
class EndJudgeMakesDecisionBusinessProcessCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EndJudgeMakesDecisionBusinessProcessCallbackHandler handler;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private StateGeneratorService stateGeneratorService;
    @MockBean
    private ParentCaseUpdateHelper parentCaseUpdateHelper;
    @MockBean
    private JudicialDecisionHelper judicialDecisionHelper;
    private CallbackParams params;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(END_JUDGE_BUSINESS_PROCESS_GASPEC);
    }

    @Test
    void shouldAddRespondentSolicitorDetail_WhenJudeOrderMakeUncloakApplication() {

        CaseData caseData = CaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(NO).build();

        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(caseData);
        when(judicialDecisionHelper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).thenReturn(true);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any())).thenReturn(CaseState.ORDER_MADE);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1))
            .updateParentApplicationVisibilityWithNewState(any(), any());

    }

    @Test
    void shouldNotAddRespondentSolicitorDetail_WhenJudeOrderMake_WithNoticeApplication() {

        CaseData caseData = CaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YES).build();

        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(caseData);
        when(judicialDecisionHelper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).thenReturn(false);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any())).thenReturn(CaseState.ORDER_MADE);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1))
            .updateParentWithGAState(any(), any());

    }

}
