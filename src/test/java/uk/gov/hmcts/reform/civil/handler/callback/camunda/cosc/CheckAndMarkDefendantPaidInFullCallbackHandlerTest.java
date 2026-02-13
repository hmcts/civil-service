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
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

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
    @Mock
    private InterestCalculator interestCalculator;

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
            objectMapper,
            interestCalculator
        );
    }

    @Test
    void shouldSetSendToCjesProcessVariableToFalse_whenDefendantPaidInFull() {
        CaseData caseData = CaseData.builder()
            .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_INSTANCE_ID))
            .activeJudgment(new JudgmentDetails()
                                .setFullyPaymentMadeDate(LocalDate.of(2024, 9, 20))
                                .setIssueDate(LocalDate.of(2024, 9, 9))
                                .setTotalAmount("900000")
                                .setOrderedAmount("900000"))
            .build();

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        handler.handle(params);

        verify(runtimeService, times(1)).setVariable(PROCESS_INSTANCE_ID, SEND_DETAILS_CJES, false);
        verifyNoInteractions(paidInFullJudgmentOnlineMapper);
    }

    @Test
    void shouldUpdateJudgmentAndSetSendToCjesProcessVariableToTrue_whenJudgmentPaidDateProvided() {
        LocalDate markedPaymentDate = LocalDate.of(2024, 9, 20);
        JudgmentDetails activeJudgementWithoutPayment = new JudgmentDetails()
            .setIssueDate(LocalDate.of(2024, 9, 9))
            .setTotalAmount("900000")
            .setOrderedAmount("900000");

        CaseData caseData = CaseData.builder()
            .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_INSTANCE_ID))
            .certOfSC(new CertOfSC()
                                    .setDefendantFinalPaymentDate(markedPaymentDate))
            .activeJudgment(activeJudgementWithoutPayment)
            .build();

        JudgmentDetails expected = new JudgmentDetails()
            .setIssueDate(activeJudgementWithoutPayment.getIssueDate())
            .setTotalAmount(activeJudgementWithoutPayment.getTotalAmount())
            .setOrderedAmount(activeJudgementWithoutPayment.getOrderedAmount())
            .setFullyPaymentMadeDate(markedPaymentDate)
            .setState(JudgmentState.SATISFIED);

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
        JudgmentDetails activeJudgementWithoutPayment = new JudgmentDetails()
            .setIssueDate(LocalDate.of(2024, 9, 9))
            .setTotalAmount("900000")
            .setOrderedAmount("900000");

        CaseData caseData = CaseData.builder()
            .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_INSTANCE_ID))
            .certOfSC(new CertOfSC()
                                    .setDefendantFinalPaymentDate(markedPaymentDate))
            .activeJudgment(activeJudgementWithoutPayment)
            .build();

        JudgmentDetails expected = new JudgmentDetails()
            .setIssueDate(activeJudgementWithoutPayment.getIssueDate())
            .setTotalAmount(activeJudgementWithoutPayment.getTotalAmount())
            .setOrderedAmount(activeJudgementWithoutPayment.getOrderedAmount())
            .setFullyPaymentMadeDate(markedPaymentDate)
            .setState(JudgmentState.CANCELLED);

        when(paidInFullJudgmentOnlineMapper.addUpdateActiveJudgment(caseData, markedPaymentDate)).thenReturn(expected);

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var result = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = objectMapper.convertValue(result.getData(), CaseData.class);

        assertThat(updatedData.getJoCoscRpaStatus()).isEqualTo(CANCELLED);
    }
}
