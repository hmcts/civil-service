package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.RecordJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RecordJudgmentOnlineMapper.class,
    RecordJudgmentCallbackHandler.class,
    JacksonAutoConfiguration.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class
})
class RecordJudgmentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RecordJudgmentCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoboticsAddressMapper addressMapper;

    @Test
    public void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(RECORD_JUDGMENT);
    }

    @Nested
    class AboutToStartCallback {

        @ParameterizedTest
        @EnumSource(YesOrNo.class)
        void shouldPopulateAllJoFieldsAsNull(YesOrNo yesOrNo) {
            //Given: Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment().toBuilder()
                .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
                .joIsLiveJudgmentExists(yesOrNo)
                .joIssuedDate(LocalDate.now())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            //Then: all judgmentOnline fields should be null
            assertThat(response.getData().get("joOrderMadeDate")).isNull();
            assertThat(response.getData().get("joPaymentPlan")).isNull();
            assertThat(response.getData().get("joJudgmentInstalmentDetails")).isNull();
            assertThat(response.getData().get("joJudgmentRecordReason")).isNull();
            assertThat(response.getData().get("joAmountOrdered")).isNull();
            assertThat(response.getData().get("joAmountCostOrdered")).isNull();
            assertThat(response.getData().get("joIsRegisteredWithRTL")).isNull();
            assertThat(response.getData().get("joIssuedDate")).isNull();
        }

        @Test
        void shouldNotPopulateAllJoFieldsAsNull() {
            //Given: Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment().toBuilder()
                .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
                .joIsLiveJudgmentExists(null)
                .joIssuedDate(LocalDate.now())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            //Then: it will return an empty callback
            assertThat(response.getData()).isNull();
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
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData().get("joPaymentPlan")).extracting("type").isEqualTo(PaymentPlanSelection.PAY_IN_INSTALMENTS.name());
            assertThat(response.getData().get("joInstalmentDetails")).extracting("amount").isEqualTo("120");
            assertThat(response.getData().get("joInstalmentDetails")).extracting("paymentFrequency").isEqualTo("MONTHLY");
            assertThat(response.getData().get("joInstalmentDetails")).extracting("startDate").isEqualTo("2022-12-12");
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
            assertThat(response.getData()).containsEntry("joIssuedDate", "2022-12-12");
            assertThat(response.getData().get("joJudgmentPaidInFull")).isNull();
            //Check Active Judgment
            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("rtlState").isEqualTo(JudgmentRTLStatus.ISSUED.getRtlState());
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("JUDGMENT_FOLLOWING_HEARING");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("paymentPlan").extracting("type").isEqualTo("PAY_IN_INSTALMENTS");
            assertThat(response.getData().get("activeJudgment")).extracting("orderedAmount").isEqualTo("1200");
            assertThat(response.getData().get("activeJudgment")).extracting("costs").isEqualTo("1100");
            assertThat(response.getData().get("activeJudgment")).extracting("totalAmount").isEqualTo("2300");
            assertThat(response.getData().get("activeJudgment")).extracting("issueDate").isEqualTo("2022-12-12");
            assertThat(response.getData().get("activeJudgment")).extracting("instalmentDetails").extracting("paymentFrequency").isEqualTo("MONTHLY");
            assertThat(response.getData().get("activeJudgment")).extracting("instalmentDetails").extracting("amount").isEqualTo("120");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Dob").isNotNull();

        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_Immediately() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                                                         JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData().get("joPaymentPlan")).extracting("type").isEqualTo(PaymentPlanSelection.PAY_IMMEDIATELY.name());
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
            assertThat(response.getData()).containsEntry("joIssuedDate", "2022-12-12");
            assertThat(response.getData().get("joJudgmentPaidInFull")).isNull();

            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("rtlState").isEqualTo(JudgmentRTLStatus.ISSUED.getRtlState());
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("JUDGMENT_FOLLOWING_HEARING");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("paymentPlan").extracting("type").isEqualTo(PaymentPlanSelection.PAY_IMMEDIATELY.name());
            assertThat(response.getData().get("activeJudgment")).extracting("orderedAmount").isEqualTo("1200");
            assertThat(response.getData().get("activeJudgment")).extracting("costs").isEqualTo("1100");
            assertThat(response.getData().get("activeJudgment")).extracting("totalAmount").isEqualTo("2300");
            assertThat(response.getData().get("activeJudgment")).extracting("issueDate").isEqualTo("2022-12-12");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("Mr. Sole Trader");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Dob").isNotNull();

        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_By_Date() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                                                         JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData().get("joPaymentPlan")).extracting("type").isEqualTo(PaymentPlanSelection.PAY_BY_DATE.name());
            assertThat(response.getData().get("joPaymentPlan")).extracting("paymentDeadlineDate").isEqualTo("2023-12-12");
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
            assertThat(response.getData()).containsEntry("joIssuedDate", "2022-12-12");
            assertThat(response.getData().get("joJudgmentPaidInFull")).isNull();

            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("rtlState").isEqualTo(JudgmentRTLStatus.ISSUED.getRtlState());
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("JUDGMENT_FOLLOWING_HEARING");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("paymentPlan").extracting("type").isEqualTo(PaymentPlanSelection.PAY_BY_DATE.name());
            assertThat(response.getData().get("activeJudgment")).extracting("paymentPlan").extracting("paymentDeadlineDate").isEqualTo("2023-12-12");
            assertThat(response.getData().get("activeJudgment")).extracting("orderedAmount").isEqualTo("1200");
            assertThat(response.getData().get("activeJudgment")).extracting("costs").isEqualTo("1100");
            assertThat(response.getData().get("activeJudgment")).extracting("totalAmount").isEqualTo("2300");
            assertThat(response.getData().get("activeJudgment")).extracting("issueDate").isEqualTo("2022-12-12");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("The Organisation");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_By_Date_multi_party() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate_Multi_party();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                                                         JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData().get("joPaymentPlan")).extracting("type").isEqualTo(PaymentPlanSelection.PAY_BY_DATE.name());
            assertThat(response.getData().get("joPaymentPlan")).extracting("paymentDeadlineDate").isEqualTo("2023-12-12");
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
            assertThat(response.getData()).containsEntry("joIssuedDate", "2022-12-12");
            assertThat(response.getData().get("joJudgmentPaidInFull")).isNull();

            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("rtlState").isEqualTo(JudgmentRTLStatus.ISSUED.getRtlState());
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("JUDGMENT_FOLLOWING_HEARING");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("paymentPlan").extracting("type").isEqualTo(PaymentPlanSelection.PAY_BY_DATE.name());
            assertThat(response.getData().get("activeJudgment")).extracting("paymentPlan").extracting("paymentDeadlineDate").isEqualTo("2023-12-12");
            assertThat(response.getData().get("activeJudgment")).extracting("orderedAmount").isEqualTo("1200");
            assertThat(response.getData().get("activeJudgment")).extracting("costs").isEqualTo("1100");
            assertThat(response.getData().get("activeJudgment")).extracting("totalAmount").isEqualTo("2300");
            assertThat(response.getData().get("activeJudgment")).extracting("issueDate").isEqualTo("2022-12-12");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("The Organisation");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Dob").isNotNull();
        }

        @Test
        void whenAboutToSubmit_andRTLNo_thenSetIssuedDateToNull() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
            caseData.setJoIsRegisteredWithRTL(YesOrNo.NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("joIssuedDate")).isNull();
            assertThat(response.getData().get("activeJudgment")).extracting("rtlState").isNull();
        }
    }

    @Nested
    class MidCallback {
        @Test
        void shouldValidatePaymentInstalmentDate() {

            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoInstalmentDetails(JudgmentInstalmentDetails.builder().startDate(LocalDate.now().minusDays(2)).build());

            CallbackParams params = callbackParamsOf(caseData, MID, "validateDates");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("Date of first instalment must be in the future");
        }

        @Test
        void shouldValidatePaymentPaidByDate() {

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
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
