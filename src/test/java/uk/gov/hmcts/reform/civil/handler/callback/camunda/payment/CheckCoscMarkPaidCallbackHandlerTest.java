package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHECK_PAID_IN_FULL_SCHED_DEADLINE;

@ExtendWith(MockitoExtension.class)
class CheckCoscMarkPaidCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CheckCoscMarkPaidCallbackHandler handler;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Time time;

    private final LocalDateTime nowMock = LocalDateTime.of(2024, 10, 8, 0, 0, 0);

    private final LocalDateTime expectedlocalDateTime = LocalDateTime.of(2024, 10, 8, 0, 0, 0).plusDays(30);

    @Test
    void checkIsMarkedPaidWithoutActiveJudgment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        when(time.now()).thenReturn(nowMock);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        assertEquals(expectedlocalDateTime, caseData.getCoscSchedulerDeadline());
    }
    @Test
    void checkIsMarkedPaidWithActiveJudgment() {
        when(time.now()).thenReturn(nowMock);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData.setActiveJudgment(JudgmentDetails.builder().issueDate(LocalDate.now())
                                       .paymentPlan(JudgmentPaymentPlan.builder()
                                                        .type(PaymentPlanSelection.PAY_IMMEDIATELY).build())
                                       .orderedAmount("100")
                                       .costs("50")
                                       .totalAmount("150")
                                       .build());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertEquals(expectedlocalDateTime, caseData.getCoscSchedulerDeadline());
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void checkIsMarkedPaidWithFullyPaymentMadeDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData.setActiveJudgment(JudgmentDetails.builder()
                                       .fullyPaymentMadeDate(LocalDate.of(2023, 1, 15))
                                             .build());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        assertNull(caseData.getActiveJudgment().getFullyPaymentMadeDate());

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CHECK_PAID_IN_FULL_SCHED_DEADLINE);
    }

}
