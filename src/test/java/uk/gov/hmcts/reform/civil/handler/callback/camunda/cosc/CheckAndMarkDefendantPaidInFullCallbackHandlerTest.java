package uk.gov.hmcts.reform.civil.handler.callback.camunda.cosc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentPaidInFullOnlineMapper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus.SATISFIED;
import static uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus.CANCELLED;

@ExtendWith(MockitoExtension.class)
class CheckAndMarkDefendantPaidInFullCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CheckAndMarkDefendantPaidInFullCallbackHandler handler;
    @Mock
    private JudgmentPaidInFullOnlineMapper paidInFullJudgmentOnlineMapper;
    @Mock
    private RuntimeService runtimeService;

    private static final String PROCESS_INSTANCE_ID = "process-instance-id";
    private static final String SEND_DETAILS_CJES = "sendDetailsToCJES";
    private static ObjectMapper objectMapper;
    private final LocalDateTime nowMock = LocalDateTime.of(2024, 10, 8, 0, 0, 0);

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new CheckAndMarkDefendantPaidInFullCallbackHandler(
            paidInFullJudgmentOnlineMapper,
            runtimeService,
            objectMapper
        );
    }

    @Test
    void shouldSetSendToCjesProcessVariableToFalse_whenDefendantPaidInFull() {
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .activeJudgment(JudgmentDetails.builder()
                                .fullyPaymentMadeDate(LocalDate.of(2024, 9, 20))
                                .issueDate(LocalDate.of(2024, 9, 9))
                                .totalAmount("900000")
                                .orderedAmount("900000")
                                .build())
            .build();

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        handler.handle(params);

        verify(runtimeService, times(1)).setVariable(PROCESS_INSTANCE_ID, SEND_DETAILS_CJES, false);
        verifyNoInteractions(paidInFullJudgmentOnlineMapper);
    }

    @Test
    void shouldUpdateJudgmentAndSetSendToCjesProcessVariableToTrue_whenJudgmentPaidDateProvided() {
        LocalDate markedPaymentDate = LocalDate.of(2024, 9, 20);
        JudgmentDetails activeJudgementWithoutPayment = JudgmentDetails.builder()
            .issueDate(LocalDate.of(2024, 9, 9))
            .totalAmount("900000")
            .orderedAmount("900000")
            .build();

        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .certOfSC(CertOfSC.builder()
                                    .defendantFinalPaymentDate(markedPaymentDate)
                                    .build())
            .activeJudgment(activeJudgementWithoutPayment)
            .build();

        JudgmentDetails expected = caseData.getActiveJudgment().toBuilder()
            .fullyPaymentMadeDate(markedPaymentDate)
            .state(JudgmentState.SATISFIED)
            .build();

        when(paidInFullJudgmentOnlineMapper.addUpdateActiveJudgment(caseData, markedPaymentDate)).thenReturn(expected);

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var result = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = objectMapper.convertValue(result.getData(), CaseData.class);

        assertEquals(expected, updatedData.getActiveJudgment());
        assertThat(updatedData.getJoCoscRpaStatus()).isEqualTo(SATISFIED);
        verify(runtimeService, times(1)).setVariable(PROCESS_INSTANCE_ID, SEND_DETAILS_CJES, true);
        assertThat(updatedData.getJoDefendantMarkedPaidInFullIssueDate()).isNotNull();
    }

    @Test
    void shouldSetJudgementStateAsCancelled() {
        LocalDate markedPaymentDate = LocalDate.of(2024, 9, 20);
        JudgmentDetails activeJudgementWithoutPayment = JudgmentDetails.builder()
            .issueDate(LocalDate.of(2024, 9, 9))
            .totalAmount("900000")
            .orderedAmount("900000")
            .build();

        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .certOfSC(CertOfSC.builder()
                                    .defendantFinalPaymentDate(markedPaymentDate)
                                    .build())
            .activeJudgment(activeJudgementWithoutPayment)
            .build();

        JudgmentDetails expected = caseData.getActiveJudgment().toBuilder()
            .fullyPaymentMadeDate(markedPaymentDate)
            .state(JudgmentState.CANCELLED)
            .build();

        when(paidInFullJudgmentOnlineMapper.addUpdateActiveJudgment(caseData, markedPaymentDate)).thenReturn(expected);

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var result = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = objectMapper.convertValue(result.getData(), CaseData.class);

        assertThat(updatedData.getJoCoscRpaStatus()).isEqualTo(CANCELLED);
    }
}
