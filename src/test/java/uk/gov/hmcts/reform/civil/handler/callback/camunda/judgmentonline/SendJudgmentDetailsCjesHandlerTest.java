package uk.gov.hmcts.reform.civil.handler.callback.camunda.judgmentonline;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.judgments.ReportJudgmentsService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_JUDGMENT_DETAILS_CJES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_JUDGMENT_DETAILS_CJES_SA;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason.DETERMINATION_OF_MEANS;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason.JUDGE_ORDER;

class SendJudgmentDetailsCjesHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private ReportJudgmentsService reportJudgmentsService;

    @InjectMocks
    private SendJudgmentDetailsCjesHandler sendJudgmentDetailsCjesHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSendJudgmentDetailsWhenCaseEventIsSendJudgmentDetailsCJES() {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .joIsRegisteredWithRTL(YES)
            .joJudgmentRecordReason(DETERMINATION_OF_MEANS)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES.name());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) sendJudgmentDetailsCjesHandler.handle(params);

        // Assert
        verify(reportJudgmentsService).sendJudgment(eq(caseData), eq(true));
        verify(runtimeService).setVariable(processId, "judgmentRecordedReason", DETERMINATION_OF_MEANS.toString());
    }

    @Test
    void shouldSendJudgmentDetailsWhenCaseEventIsSendJudgmentDetailsCjesSetAside() {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .joIsRegisteredWithRTL(YES)
            .joJudgmentRecordReason(JUDGE_ORDER)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES_SA.name());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) sendJudgmentDetailsCjesHandler.handle(params);

        // Assert
        verify(reportJudgmentsService).sendJudgment(eq(caseData), eq(false));
        verify(runtimeService).setVariable(processId, "judgmentRecordedReason", JUDGE_ORDER.toString());
    }

    @Test
    void shouldNotSendJudgmentDetailsWhenJoIsRegisteredWithRTLIsNo() {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .joIsRegisteredWithRTL(null)
            .joJudgmentRecordReason(JUDGE_ORDER)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES_SA.name());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) sendJudgmentDetailsCjesHandler.handle(params);

        verify(reportJudgmentsService, never()).sendJudgment(any(), any());
    }
}
