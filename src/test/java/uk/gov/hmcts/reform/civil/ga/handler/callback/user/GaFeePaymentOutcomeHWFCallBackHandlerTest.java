package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaPaymentRequestUpdateCallbackService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.ga.service.HwfNotificationService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FEE_PAYMENT_OUTCOME_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_COSC_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_ADD_HWF;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GaFeePaymentOutcomeHWFCallBackHandler.class,
    JacksonAutoConfiguration.class
})
public class GaFeePaymentOutcomeHWFCallBackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    private GaFeePaymentOutcomeHWFCallBackHandler handler;
    @Autowired
    private ObjectMapper mapper = new ObjectMapper();
    @MockBean
    private GaPaymentRequestUpdateCallbackService service;
    @MockBean
    private HwfNotificationService hwfNotificationService;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(FEE_PAYMENT_OUTCOME_GA);
    }

    @Nested
    class AboutToStartCallbackHandling {

        @Test
        void updateFeeType_shouldSetAdditionalFeeTypeWithEmptyRef_whenCaseStateIsApplicationAddPayment() {
            // Arrange
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .generalAppHelpWithFees(HelpWithFees.builder().build())
                .hwfFeeType(FeeType.ADDITIONAL)
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().fee(
                    Fee.builder()
                        .calculatedAmountInPence(BigDecimal.valueOf(180))
                        .code("FEE123").build()).build())
                .build();

            // Act
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            // Assert
            assertThat(updatedData.getHwfFeeType()).isEqualTo(FeeType.ADDITIONAL);
            assertThat(updatedData.getFeePaymentOutcomeDetails().getHwfNumberAvailable()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void updateFeeType_shouldSetAdditionalFeeTypeWithRef_whenCaseStateIsApplicationAddPayment() {
            // Arrange
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .hwfFeeType(FeeType.ADDITIONAL)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("123").build())
                .build();

            // Act
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            // Assert
            assertThat(updatedData.getHwfFeeType()).isEqualTo(FeeType.ADDITIONAL);
            assertThat(updatedData.getFeePaymentOutcomeDetails().getHwfNumberAvailable()).isEqualTo(YesOrNo.YES);
            assertThat(updatedData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome()).isEqualTo("123");
        }

        @Test
        void updateFeeType_shouldSetApplicationFeeTypeWithEmptyRef_whenCaseStateIsNotApplicationAddPayment() {
            // Arrange
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
                .hwfFeeType(FeeType.APPLICATION)
                .generalAppHelpWithFees(HelpWithFees.builder().build())
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().fee(
                    Fee.builder()
                        .calculatedAmountInPence(BigDecimal.valueOf(180))
                        .code("FEE123").build()).build())
                .build();

            // Act
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            // Assert
            assertThat(updatedData.getHwfFeeType()).isEqualTo(FeeType.APPLICATION);
            assertThat(updatedData.getFeePaymentOutcomeDetails().getHwfNumberAvailable()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void updateFeeType_shouldSetApplicationFeeTypeWithRef_whenCaseStateIsNotApplicationAddPayment() {
            // Arrange
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
                .hwfFeeType(FeeType.APPLICATION)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("123").build())
                .build();

            // Act
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            // Assert
            assertThat(updatedData.getHwfFeeType()).isEqualTo(FeeType.APPLICATION);
            assertThat(updatedData.getFeePaymentOutcomeDetails().getHwfNumberAvailable()).isEqualTo(YesOrNo.YES);
            assertThat(updatedData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome()).isEqualTo("123");
        }
    }

    @Nested
    class MidCallback {

        @Test
        void shouldValidationFeePaymentOutcomeGa_withInvalidOutstandingFee() {
            //Given
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                    .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                            .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                            .hwfFullRemissionGrantedForGa(YesOrNo.YES).build())
                    .hwfFeeType(FeeType.APPLICATION)
                    .gaHwfDetails(HelpWithFeesDetails.builder()
                            .outstandingFee(BigDecimal.valueOf(100.00))
                            .build())
                    .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, MID, "remission-type");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertThat(response.getErrors()).containsExactly("Incorrect remission type selected");
        }

        @Test
        void shouldValidationFeePaymentOutcomeAdditional_withInvalidOutstandingFee() {
            //Given
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                    .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                            .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                            .hwfFullRemissionGrantedForAdditionalFee(YesOrNo.YES).build())
                    .hwfFeeType(FeeType.ADDITIONAL)
                    .additionalHwfDetails(HelpWithFeesDetails.builder()
                            .outstandingFee(BigDecimal.valueOf(100.00))
                            .build())
                    .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, MID, "remission-type");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertThat(response.getErrors()).containsExactly("Incorrect remission type selected");
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldTrigger_after_payment_GaFee() {
            List<GeneralApplicationTypes> types = Arrays.asList(STRIKE_OUT);
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                    .generalAppPBADetails(GeneralApplicationPbaDetails.builder().fee(
                                    Fee.builder()
                                            .calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
                            .build())
                    .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("ref").build())
                    .gaHwfDetails(HelpWithFeesDetails.builder().build())
                .generalAppType(GAApplicationType.builder().types(types).build())
                    .hwfFeeType(FeeType.APPLICATION)
                    .build();
            when(service.processHwf(any(GeneralApplicationCaseData.class)))
                    .thenAnswer((Answer<GeneralApplicationCaseData>) invocation -> invocation.getArgument(0));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            verify(service, times(1)).processHwf(any());
            verify(hwfNotificationService, times(1)).sendNotification(any(), eq(FEE_PAYMENT_OUTCOME_GA));
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT.toString());
        }

        @Test
        void shouldTrigger_after_payment_GaFee_shouldTriggerCosc() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                    .generalAppType(GAApplicationType.builder()
                                        .types(List.of(GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID))
                                        .build())
                    .generalAppPBADetails(GeneralApplicationPbaDetails.builder().fee(
                                    Fee.builder()
                                            .calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
                            .build())
                    .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("ref").build())
                    .gaHwfDetails(HelpWithFeesDetails.builder().build())
                    .hwfFeeType(FeeType.APPLICATION)
                    .build();
            when(service.processHwf(any(GeneralApplicationCaseData.class)))
                    .thenAnswer((Answer<GeneralApplicationCaseData>) invocation -> invocation.getArgument(0));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            verify(service, times(1)).processHwf(any());
            verify(hwfNotificationService, times(1)).sendNotification(any(), eq(FEE_PAYMENT_OUTCOME_GA));
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(INITIATE_COSC_APPLICATION_AFTER_PAYMENT.toString());
        }

        @Test
        void shouldTrigger_modify_state_additioanlFee() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                    .generalAppPBADetails(GeneralApplicationPbaDetails.builder().fee(
                                    Fee.builder()
                                            .calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
                            .build())
                    .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("ref").build())
                    .gaHwfDetails(HelpWithFeesDetails.builder().build())
                    .additionalHwfDetails(HelpWithFeesDetails.builder().build())
                    .hwfFeeType(FeeType.ADDITIONAL)
                    .build();
            when(service.processHwf(any(GeneralApplicationCaseData.class)))
                    .thenAnswer((Answer<GeneralApplicationCaseData>) invocation -> invocation.getArgument(0));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            verify(service, times(1)).processHwf(any());
            verify(hwfNotificationService, times(1)).sendNotification(any(), eq(FEE_PAYMENT_OUTCOME_GA));
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(UPDATE_GA_ADD_HWF.toString());
        }
    }
}
