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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EDIT_JUDGMENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EditJudgmentCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class EditJudgmentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EditJudgmentCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(EDIT_JUDGMENT);
    }

    @Nested
    class AboutToStartCallback {

        @ParameterizedTest
        @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
        void shouldPopulateIfRTLRadioDisplay(YesOrNo value) {
            //Given: Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoIsRegisteredWithRTL(value);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            //Then: all showRTL field should be set correctly
            if (value == YesOrNo.NO) {
                assertThat(response.getData()).containsEntry("joShowRegisteredWithRTLOption", "Yes");
            } else {
                assertThat(response.getData()).containsEntry("joShowRegisteredWithRTLOption", "No");
            }
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_Instalment_WITH_RTL_YES_TO_YES() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State and RTL is Yes in active judgment
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoJudgmentStatusDetails(JudgmentStatusDetails.builder()
                                                    .judgmentStatusTypes(JudgmentStatusType.ISSUED)
                                                    .joRtlState(JudgmentRTLStatus.REGISTRATION.getRtlState())
                                                    .lastUpdatedDate(LocalDateTime.now()).build());
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("judgmentStatusTypes").isEqualTo(
                JudgmentStatusType.MODIFIED.name());
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("joRtlState").isEqualTo("M");
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("lastUpdatedDate").isNotNull();
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData()).containsEntry("joPaymentPlanSelection",
                                                         PaymentPlanSelection.PAY_IN_INSTALMENTS.name());
            assertThat(response.getData().get("joJudgmentInstalmentDetails")).extracting("instalmentAmount").isEqualTo("120");
            assertThat(response.getData().get("joJudgmentInstalmentDetails")).extracting("paymentFrequency").isEqualTo("MONTHLY");
            assertThat(response.getData().get("joJudgmentInstalmentDetails")).extracting("firstInstalmentDate").isEqualTo("2022-12-12");
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joIsRegisteredWithRTL", "Yes");
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
            assertThat(response.getData()).containsEntry("joIssuedDate", "2022-12-12");
            assertThat(response.getData().get("joJudgmentPaidInFull")).isNull();
        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_Immediately_RTL_NO_TO_YES() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
            caseData.setJoJudgmentStatusDetails(JudgmentStatusDetails.builder()
                                                    .judgmentStatusTypes(JudgmentStatusType.ISSUED)
                                                    .joRtlState(JudgmentRTLStatus.REGISTRATION.getRtlState())
                                                    .lastUpdatedDate(LocalDateTime.now()).build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.YES);
            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("judgmentStatusTypes").isEqualTo(
                JudgmentStatusType.MODIFIED.name());
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
            assertThat(response.getData()).containsEntry("joIssuedDate", "2022-12-12");
            assertThat(response.getData().get("joJudgmentPaidInFull")).isNull();
        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_By_Date_RTL_NO_TO_NO() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
            caseData.setJoIsRegisteredWithRTL(YesOrNo.NO);
            caseData.setJoJudgmentStatusDetails(JudgmentStatusDetails.builder()
                                                    .judgmentStatusTypes(JudgmentStatusType.ISSUED)
                                                    .lastUpdatedDate(LocalDateTime.now()).build());
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("judgmentStatusTypes").isEqualTo(
                JudgmentStatusType.MODIFIED.name());
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("joRtlState").isNull();
            assertThat(response.getData().get("joJudgmentStatusDetails")).extracting("lastUpdatedDate").isNotNull();
            assertThat(response.getData()).containsEntry("joJudgmentRecordReason",
                                                         JudgmentRecordedReason.JUDGE_ORDER.name());
            assertThat(response.getData()).containsEntry("joPaymentPlanSelection",
                PaymentPlanSelection.PAY_BY_DATE.name());
            assertThat(response.getData()).containsEntry("joAmountOrdered", "1200");
            assertThat(response.getData()).containsEntry("joAmountCostOrdered", "1100");
            assertThat(response.getData()).containsEntry("joOrderMadeDate", "2022-12-12");
            assertThat(response.getData()).containsEntry("joPaymentToBeMadeByDate", "2023-12-12");
            assertThat(response.getData().get("joIssuedDate")).isNull();
            assertThat(response.getData().get("joJudgmentPaidInFull")).isNull();
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

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
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

        @Test
        void shouldNotThrowErrorWhenAllDatesAreValid() {

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
            caseData.setJoOrderMadeDate(LocalDate.now().minusDays(2));
            caseData.setJoPaymentToBeMadeByDate(LocalDate.now().plusDays(2));

            CallbackParams params = callbackParamsOf(caseData, MID, "validateDates");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void whenSubmitted_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            Assertions.assertTrue(response.getConfirmationHeader().contains("# Judgment edited"));
            Assertions.assertTrue(response.getConfirmationBody().contains("The judgment has been edited"));
        }
    }
}
