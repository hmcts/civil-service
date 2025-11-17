package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.DefaultJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.EditJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.RecordJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EDIT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class EditJudgmentCallbackHandlerTest extends BaseCallbackHandlerTest {

    private EditJudgmentCallbackHandler handler;

    @Mock
    private InterestCalculator interestCalculator;

    private DefaultJudgmentOnlineMapper defaultJudgmentOnlineMapper;

    @Mock
    private RoboticsAddressMapper addressMapper;

    @Mock
    Time time;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        defaultJudgmentOnlineMapper = new DefaultJudgmentOnlineMapper(time, interestCalculator, addressMapper);
        EditJudgmentOnlineMapper editJudgmentOnlineMapper = new EditJudgmentOnlineMapper(time);
        handler = new EditJudgmentCallbackHandler(objectMapper, editJudgmentOnlineMapper, interestCalculator);
    }

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
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoIsRegisteredWithRTL(value);
            RecordJudgmentOnlineMapper recordMapper = new RecordJudgmentOnlineMapper(time, addressMapper);
            caseData.setActiveJudgment(recordMapper.addUpdateActiveJudgment(caseData));
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

        @ParameterizedTest
        @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
        void shouldPopulateIfRTLRadioDisplayForDJ() {
            //Given: Casedata in All_FINAL_ORDERS_ISSUED State
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPaymentAmount("10")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            caseData.setActiveJudgment(defaultJudgmentOnlineMapper.addUpdateActiveJudgment(caseData));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            //Then: all showRTL field should be set correctly
            assertThat(response.getData()).containsEntry("joShowRegisteredWithRTLOption", "No");
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_Instalment_WITH_RTL_YES_TO_YES() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State and RTL is Yes in active judgment
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            when(interestCalculator.calculateInterest(any())).thenReturn(BigDecimal.ZERO);
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.NO);
            RecordJudgmentOnlineMapper recordMapper = new RecordJudgmentOnlineMapper(time, addressMapper);
            caseData.setActiveJudgment(recordMapper.addUpdateActiveJudgment(caseData));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData()).containsEntry(
                "joJudgmentRecordReason",
                JudgmentRecordedReason.JUDGE_ORDER.name()
            );
            assertThat(response.getData().get("joPaymentPlan")).isNotNull();
            assertThat(response.getData().get("joInstalmentDetails")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("joRepaymentSummaryObject")).isNotNull();
        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_Immediately_RTL_NO_TO_YES() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            when(interestCalculator.calculateInterest(any())).thenReturn(BigDecimal.ZERO);
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.YES);
            RecordJudgmentOnlineMapper recordMapper = new RecordJudgmentOnlineMapper(time, addressMapper);
            caseData.setActiveJudgment(recordMapper.addUpdateActiveJudgment(caseData));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData()).containsEntry(
                "joJudgmentRecordReason",
                JudgmentRecordedReason.JUDGE_ORDER.name()
            );
            assertThat(response.getData().get("joPaymentPlan")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("joRepaymentSummaryObject")).isNotNull();
        }

        @Test
        void shouldPopulateAllJudgmentFields_For_Pay_By_Date_RTL_NO_TO_NO() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            when(interestCalculator.calculateInterest(any())).thenReturn(BigDecimal.ZERO);
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
            caseData.setJoIsRegisteredWithRTL(YesOrNo.NO);
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.YES);
            RecordJudgmentOnlineMapper recordMapper = new RecordJudgmentOnlineMapper(time, addressMapper);
            caseData.setActiveJudgment(recordMapper.addUpdateActiveJudgment(caseData));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getData()).containsEntry(
                "joJudgmentRecordReason",
                JudgmentRecordedReason.JUDGE_ORDER.name()
            );
            assertThat(response.getData().get("joPaymentPlan")).isNotNull();
            assertThat(response.getData().get("activeJudgment")).isNotNull();
            assertThat(response.getData().get("joRepaymentSummaryObject")).isNotNull();
            // When RTL is NO, joIssuedDate should be null
            assertThat(response.getData().get("joIssuedDate")).isNull();
        }

        @Test
        void shouldThrowErrorIfNoActiveJudgment() {

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
            caseData.setJoIsRegisteredWithRTL(YesOrNo.NO);
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: judgmentOnline fields should be set correctly
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly("There is no active judgment to edit");

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

        @Test
        void shouldNotThrowErrorWhenAllDatesAreValid() {

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
            caseData.setJoOrderMadeDate(LocalDate.now().minusDays(2));
            caseData.setJoPaymentPlan(JudgmentPaymentPlan.builder()
                                          .type(PaymentPlanSelection.PAY_BY_DATE)
                                          .paymentDeadlineDate(LocalDate.now().plusDays(2)).build());

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
