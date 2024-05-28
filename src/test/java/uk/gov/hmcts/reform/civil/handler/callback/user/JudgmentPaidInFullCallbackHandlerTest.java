package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGMENT_PAID_IN_FULL;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    JudgmentPaidInFullCallbackHandler.class
})
class JudgmentPaidInFullCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private JudgmentPaidInFullCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

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
            caseData.setActiveJudgment(JudgmentDetails.builder().build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then: judgmentOnline fields should be set correctly

            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("dateOfFullPaymentMade").isEqualTo("2023-04-02");
            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("confirmFullPaymentMade").isEqualTo(List.of("CONFIRMED"));

            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("SATISFIED");
            assertThat(response.getData().get("activeJudgment")).extracting("fullyPaymentMadeDate").isEqualTo("2023-04-02");
        }

        @Test
        void shouldPopulateJudgementStatusAsSatisfied() {
            //Given: Casedata is in All_FINAL_ORDERS_ISSUED State and Record Judgement is done
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
            caseData.setActiveJudgment(JudgmentDetails.builder().build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then: judgmentOnline fields should be set correctly

            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("dateOfFullPaymentMade").isEqualTo("2023-04-02");
            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("confirmFullPaymentMade").isEqualTo(List.of("CONFIRMED"));
            assertThat(response.getData().get("joIsLiveJudgmentExists")).isEqualTo("No");

        }

        @Test
        void shouldPopulateJudgementStatusAsCancelled() {
            //Given: Casedata is in All_FINAL_ORDERS_ISSUED State and Record Judgement is done
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31Days();
            caseData.setActiveJudgment(JudgmentDetails.builder().build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then: judgmentOnline fields should be set correctly

            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("dateOfFullPaymentMade").isEqualTo("2023-04-01");
            assertThat(response.getData().get("joJudgmentPaidInFull")).extracting("confirmFullPaymentMade").isEqualTo(List.of("CONFIRMED"));
            assertThat(response.getData().get("joIsLiveJudgmentExists")).isEqualTo("No");

            assertThat(response.getData().get("activeJudgment")).isNull();
            assertThat(response.getData().get("historicJudgment")).isNotNull();
            JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
            assertEquals(JudgmentState.CANCELLED, historicJudgment.getState());
            assertEquals(LocalDate.now(), historicJudgment.getCancelDate());
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
