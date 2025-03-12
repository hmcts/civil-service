package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentPaidInFullOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGMENT_PAID_IN_FULL;

@ExtendWith(MockitoExtension.class)
class JudgmentPaidInFullCallbackHandlerTest extends BaseCallbackHandlerTest {

    private JudgmentPaidInFullCallbackHandler handler;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JudgmentPaidInFullOnlineMapper paidInFullJudgmentOnlineMapper = new JudgmentPaidInFullOnlineMapper();
        handler = new JudgmentPaidInFullCallbackHandler(objectMapper, paidInFullJudgmentOnlineMapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(JUDGMENT_PAID_IN_FULL);
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldPopulateDate() {
            //Given: Casedata is in All_FINAL_ORDERS_ISSUED State and Record Judgement is done
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
            caseData.setActiveJudgment(JudgmentDetails.builder().issueDate(LocalDate.now())
                                           .paymentPlan(JudgmentPaymentPlan.builder()
                                                            .type(PaymentPlanSelection.PAY_IMMEDIATELY).build())
                                           .orderedAmount("100")
                                           .costs("50")
                                           .totalAmount("150")
                                           .build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then: judgmentOnline fields should be set correctly

            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("dateOfFullPaymentMade").isEqualTo(LocalDate.now().plusDays(35).toString());
            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("confirmFullPaymentMade").isEqualTo(List.of("CONFIRMED"));

            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("SATISFIED");
            assertThat(response.getData().get("activeJudgment")).extracting("fullyPaymentMadeDate").isEqualTo(LocalDate.now().plusDays(35).toString());

            assertThat(response.getData().get("joCoscRpaStatus")).isEqualTo("SATISFIED");
            assertThat(response.getData().get("joMarkedPaidInFullIssueDate")).isNotNull();
        }

        @Test
        void shouldPopulateJudgementStatusAsSatisfied() {
            //Given: Casedata is in All_FINAL_ORDERS_ISSUED State and Record Judgement is done
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
            caseData.setActiveJudgment(JudgmentDetails.builder().issueDate(LocalDate.now())
                                           .paymentPlan(JudgmentPaymentPlan.builder()
                                                            .type(PaymentPlanSelection.PAY_IMMEDIATELY).build())
                                           .orderedAmount("100")
                                           .costs("50")
                                           .totalAmount("150")
                                           .build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then: judgmentOnline fields should be set correctly

            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("dateOfFullPaymentMade").isEqualTo(LocalDate.now().plusDays(35).toString());
            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("confirmFullPaymentMade").isEqualTo(List.of("CONFIRMED"));
            assertThat(response.getData().get("joIsLiveJudgmentExists")).isEqualTo("Yes");

            assertThat(response.getData().get("joCoscRpaStatus")).isEqualTo("SATISFIED");
            assertThat(response.getData().get("joMarkedPaidInFullIssueDate")).isNotNull();
        }

        @Test
        void shouldPopulateJudgementStatusAsCancelled() {
            //Given: Casedata is in All_FINAL_ORDERS_ISSUED State and Record Judgement is done
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31Days();
            caseData.setActiveJudgment(JudgmentDetails.builder().issueDate(LocalDate.now())
                                           .paymentPlan(JudgmentPaymentPlan.builder()
                                                            .type(PaymentPlanSelection.PAY_IMMEDIATELY)
                                                            .build())
                                           .orderedAmount("100")
                                           .costs("50")
                                           .totalAmount("150")
                                           .build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then: judgmentOnline fields should be set correctly

            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("dateOfFullPaymentMade").isEqualTo(LocalDate.now().plusDays(15).toString());
            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("confirmFullPaymentMade").isEqualTo(List.of("CONFIRMED"));
            assertThat(response.getData().get("joIsLiveJudgmentExists")).isEqualTo("Yes");

            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("historicJudgment")).isNull();

            assertThat(response.getData().get("joCoscRpaStatus")).isEqualTo("CANCELLED");
            assertThat(response.getData().get("joMarkedPaidInFullIssueDate")).isNotNull();
        }

        @Test
        void shouldPopulateJudgementStatusAsCancelledForDefaultJudgment() {
            //Given: Casedata is in All_FINAL_ORDERS_ISSUED State and Record Judgement is done
            CaseData caseData = CaseDataBuilder.builder()
                .getDefaultJudgment1v1CaseJudgmentPaid();
            caseData.setActiveJudgment(JudgmentDetails.builder().issueDate(LocalDate.now())
                                           .paymentPlan(JudgmentPaymentPlan.builder()
                                                            .type(PaymentPlanSelection.PAY_IMMEDIATELY).build())
                                           .orderedAmount("100")
                                           .costs("50")
                                           .totalAmount("150")
                                           .build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("confirmFullPaymentMade").isEqualTo(List.of("CONFIRMED"));
            assertThat(response.getData().get("joIsLiveJudgmentExists")).isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("historicJudgment")).isNull();

            assertThat(response.getData().get("joCoscRpaStatus")).isEqualTo("CANCELLED");
            assertThat(response.getData().get("joMarkedPaidInFullIssueDate")).isNotNull();
        }
    }

    @Nested
    class MidCallback {
        @Test
        void shouldValidatePaymentMadeDate() {

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
            caseData.getJoJudgmentPaidInFull().setDateOfFullPaymentMade(LocalDate.now().minusDays(2));

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-payment-date");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().contains("Date must be in past"));
        }

        @Test
        void shouldValidateJudgementDate() {

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
            caseData.getJoJudgmentPaidInFull().setDateOfFullPaymentMade(LocalDate.now().minusDays(2));
            caseData.setJoJudgementByAdmissionIssueDate(LocalDateTime.now());

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-payment-date");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().contains("Paid in full date must be on or after the date of the judgment"));
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        public void whenSubmitted_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            Assertions.assertTrue(response.getConfirmationHeader().contains("# Judgment marked as paid in full"));
            Assertions.assertTrue(response.getConfirmationBody().contains("The judgment has been marked as paid in full"));
        }
    }
}
