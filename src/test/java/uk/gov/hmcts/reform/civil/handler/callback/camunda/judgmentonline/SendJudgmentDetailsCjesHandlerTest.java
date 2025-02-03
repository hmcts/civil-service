package uk.gov.hmcts.reform.civil.handler.callback.camunda.judgmentonline;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.service.judgments.CjesService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_JUDGMENT_DETAILS_CJES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_JUDGMENT_DETAILS_CJES_SA;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason.DETERMINATION_OF_MEANS;

class SendJudgmentDetailsCjesHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private CjesService cjesService;

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
            .activeJudgment(JudgmentDetails.builder()
                                .isRegisterWithRTL(YES)
                                .build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES.name());

        sendJudgmentDetailsCjesHandler.handle(params);

        // Assert
        verify(cjesService).sendJudgment(eq(caseData), eq(true));
        verify(runtimeService).setVariable(processId, "judgmentRecordedReason", DETERMINATION_OF_MEANS.toString());
    }

    @Test
    void shouldSendJudgmentDetailsWhenNoRecordedReasonAndCaseEventIsSendJudgmentDetailsCJES() {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .joIsRegisteredWithRTL(YES)
            .activeJudgment(JudgmentDetails.builder()
                                .isRegisterWithRTL(YES)
                                .build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES.name());

        sendJudgmentDetailsCjesHandler.handle(params);

        // Assert
        verify(cjesService).sendJudgment(eq(caseData), eq(true));
        verifyNoInteractions(runtimeService);
    }

    @Test
    void shouldNotSendJudgmentDetails_whenRTLisNo() {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .joIsRegisteredWithRTL(YES)
            .joJudgmentRecordReason(DETERMINATION_OF_MEANS)
            .activeJudgment(JudgmentDetails.builder()
                                .isRegisterWithRTL(NO)
                                .build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES.name());

        sendJudgmentDetailsCjesHandler.handle(params);

        // Assert
        verify(cjesService, never()).sendJudgment(any(), any());
        verify(runtimeService).setVariable(processId, "judgmentRecordedReason", DETERMINATION_OF_MEANS.toString());
    }

    @Test
    void shouldSendJudgmentDetailsWhenCaseEventIsSendJudgmentDetailsCjesSetAside() {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .joIsRegisteredWithRTL(YES)
            .joSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER)
            .historicJudgment(ElementUtils.wrapElements(JudgmentDetails.builder()
                                                            .isRegisterWithRTL(YES)
                                                            .build()))
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES_SA.name());

        sendJudgmentDetailsCjesHandler.handle(params);

        // Assert
        verify(cjesService).sendJudgment(eq(caseData), eq(false));
        verify(runtimeService).setVariable(processId, "JUDGMENT_SET_ASIDE_ERROR", false);
    }

    @Test
    void shouldNotSendJudgmentDetailsSA_WhenRTLisNo() {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .joIsRegisteredWithRTL(YES)
            .joSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR)
            .historicJudgment(ElementUtils.wrapElements(JudgmentDetails.builder()
                                                            .isRegisterWithRTL(NO)
                                                            .build()))
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES_SA.name());

        sendJudgmentDetailsCjesHandler.handle(params);

        // Assert
        verify(cjesService, never()).sendJudgment(any(), any());
        verify(runtimeService).setVariable(processId, "JUDGMENT_SET_ASIDE_ERROR", true);
    }

    @Test
    void shouldNotSendJudgmentDetailsWhenIsRegisteredWithRTLIsNull() {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .joSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_JUDGMENT_DETAILS_CJES_SA.name());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                                  () -> sendJudgmentDetailsCjesHandler.handle(params));

        assertEquals("Historic judgement cannot be empty or null after a judgment is set aside", e.getMessage());
        verify(cjesService, never()).sendJudgment(any(), any());
    }
}
