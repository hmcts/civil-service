package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RecordJudgmentCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class RecordJudgmentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RecordJudgmentCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(RECORD_JUDGMENT);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPopulateAllJoFieldsAsNull() {
            //Given: Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build().toBuilder()
                .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            //Then: all judgmentOnline fields should be null
            assertThat(response.getData().get("joJudgmentStatusDetails")).isNull();
            assertThat(response.getData().get("joOrderMadeDate")).isNull();
            assertThat(response.getData().get("joPaymentPlanSelection")).isNull();
            assertThat(response.getData().get("joJudgmentInstalmentDetails")).isNull();
            assertThat(response.getData().get("joJudgmentRecordReason")).isNull();
            assertThat(response.getData().get("joAmountOrdered")).isNull();
            assertThat(response.getData().get("joAmountCostOrdered")).isNull();
            assertThat(response.getData().get("joIsRegisteredWithRTL")).isNull();
            assertThat(response.getData().get("joAmountOrdered")).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_Instalment() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("judgmentStatusTypes").isEqualTo(
                JudgmentStatusType.ISSUED.name());
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("joRtlState").isEqualTo("R");
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("lastUpdatedDate").isNotNull();
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData()).containsEntry("joPaymentPlanSelection",
                PaymentPlanSelection.PAY_IN_INSTALMENTS.name());
            assertThat(response.getData().get("joJudgmentInstalmentDetails")).extracting("instalmentAmount").isEqualTo("120");
            assertThat(response.getData().get("joJudgmentInstalmentDetails")).extracting("paymentFrequency").isEqualTo("MONTHLY");
            assertThat(response.getData().get("joJudgmentInstalmentDetails")).extracting("firstInstalmentDate").isEqualTo("2022-12-12");
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_Immediately() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("judgmentStatusTypes").isEqualTo(
                JudgmentStatusType.ISSUED.name());
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("joRtlState").isEqualTo("R");
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("lastUpdatedDate").isNotNull();
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                                                         JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData()).containsEntry("joPaymentPlanSelection",
                PaymentPlanSelection.PAY_IMMEDIATELY.name());
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_By_Date() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByDate();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("judgmentStatusTypes").isEqualTo(
                JudgmentStatusType.ISSUED.name());
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("joRtlState").isEqualTo("R");
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("lastUpdatedDate").isNotNull();
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                                                         JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData()).containsEntry("joPaymentPlanSelection",
                PaymentPlanSelection.PAY_BY_DATE.name());
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
            assertThat(response.getData()).containsEntry("joPaymentToBeMadeByDate", "2023-12-12");
        }
    }

    @Nested
    class MidCallback {
        @Test
        void shouldValidatePaymentInstalmentDate() {

            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoJudgmentInstalmentDetails(JudgmentInstalmentDetails.builder().firstInstalmentDate(LocalDate.now().minusDays(2)).build());

            CallbackParams params = callbackParamsOf(caseData, MID, "validateDates");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("Date of first instalment must be in the future");
        }

        @Test
        void shouldValidatePaymentPaidByDate() {

            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByDate();
            caseData.setJoPaymentToBeMadeByDate(LocalDate.now().minusDays(2));

            CallbackParams params = callbackParamsOf(caseData, MID, "validateDates");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("Date the judgment will be paid by must be in the future");
        }

        @Test
        void shouldValidateOrderDate() {

            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoOrderMadeDate(LocalDate.now().plusDays(2));

            CallbackParams params = callbackParamsOf(caseData, MID, "validateDates");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("Date judge made the order must be in the past");
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        public void whenSubmitted_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            Assertions.assertTrue(response.getConfirmationHeader().contains("# Judgment recorded"));
            Assertions.assertTrue(response.getConfirmationBody().contains("The judgment has been recorded"));
        }
    }

}
