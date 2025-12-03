package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.citizen.HWFFeePaymentOutcomeService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FEE_PAYMENT_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.FeePaymentOutcomeHWFCallBackHandler.WRONG_REMISSION_TYPE_SELECTED;

@ExtendWith(MockitoExtension.class)
public class FeePaymentOutcomeHWFCallBackHandlerTest extends BaseCallbackHandlerTest {

    private FeePaymentOutcomeHWFCallBackHandler handler;

    @Mock
    private HWFFeePaymentOutcomeService hwfService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new FeePaymentOutcomeHWFCallBackHandler(objectMapper, hwfService);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdateTheDataBaseWithHWFRefNumber_WhenFeeType_ClaimIssue() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseDataLip(CaseDataLiP.builder()
                                 .helpWithFees(HelpWithFees.builder()
                                                   .helpWithFee(YesOrNo.NO).build()).build())
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                                              .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                              .hwfFullRemissionGrantedForClaimIssue(YesOrNo.YES).build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();
            when(hwfService.updateHwfReferenceNumber(any(CaseData.class)))
                .thenAnswer((Answer<CaseData>) invocation -> invocation.getArgument(0));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            verify(hwfService, times(1)).updateHwfReferenceNumber(any());
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(CREATE_CLAIM_SPEC_AFTER_PAYMENT.toString());
        }

        @Test
        void shouldUpdateTheDataBaseWithHWFRefNumber_WhenFeeType_Hearing() {
            FeePaymentOutcomeDetails feePaymentDetails = new FeePaymentOutcomeDetails();
            feePaymentDetails.setHwfNumberAvailable(YesOrNo.YES);
            feePaymentDetails.setHwfNumberForFeePaymentOutcome("HWF-1C4-E34");
            feePaymentDetails.setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES);
            HelpWithFeesDetails hearingHwfDetails = new HelpWithFeesDetails();
            hearingHwfDetails.setHwfCaseEvent(FEE_PAYMENT_OUTCOME);
            CaseData caseData = CaseData.builder().build();
            caseData.setFeePaymentOutcomeDetails(feePaymentDetails);
            caseData.setHearingHwfDetails(hearingHwfDetails);
            caseData.setHwfFeeType(FeeType.HEARING);
            when(hwfService.updateHwfReferenceNumber(any(CaseData.class)))
                .thenAnswer((Answer<CaseData>) invocation -> invocation.getArgument(0));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            verify(hwfService, times(1)).updateHwfReferenceNumber(any());
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(CITIZEN_HEARING_FEE_PAYMENT.toString());
        }

        @Test
        void shouldValidateRemissionTypeForHearingFee() {
            FeePaymentOutcomeDetails feePaymentDetails = new FeePaymentOutcomeDetails();
            feePaymentDetails.setHwfNumberAvailable(YesOrNo.YES);
            feePaymentDetails.setHwfNumberForFeePaymentOutcome("HWF-1C4-E34");
            feePaymentDetails.setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES);
            HelpWithFeesDetails hearingHwfDetails = new HelpWithFeesDetails();
            hearingHwfDetails.setOutstandingFeeInPounds(BigDecimal.valueOf(100.00));
            CaseData caseData = CaseData.builder().build();
            caseData.setFeePaymentOutcomeDetails(feePaymentDetails);
            caseData.setHwfFeeType(FeeType.HEARING);
            caseData.setHearingHwfDetails(hearingHwfDetails);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-type");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(WRONG_REMISSION_TYPE_SELECTED);
        }

        @Test
        void shouldValidateRemissionTypeForClaimIssFee() {
            FeePaymentOutcomeDetails feePaymentDetails = new FeePaymentOutcomeDetails();
            feePaymentDetails.setHwfNumberAvailable(YesOrNo.YES);
            feePaymentDetails.setHwfNumberForFeePaymentOutcome("HWF-1C4-E34");
            feePaymentDetails.setHwfFullRemissionGrantedForClaimIssue(YesOrNo.YES);
            HelpWithFeesDetails claimIssuedHwfDetails = new HelpWithFeesDetails();
            claimIssuedHwfDetails.setOutstandingFeeInPounds(BigDecimal.valueOf(100.00));
            CaseData caseData = CaseData.builder().build();
            caseData.setFeePaymentOutcomeDetails(feePaymentDetails);
            caseData.setHwfFeeType(FeeType.CLAIMISSUED);
            caseData.setClaimIssuedHwfDetails(claimIssuedHwfDetails);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-type");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(WRONG_REMISSION_TYPE_SELECTED);
        }

        @Test
        void shouldUpdateBusinessProcess_WhenFeeType_ClaimIssue() {
            CaseData caseData = CaseData.builder().build();
            caseData.setHwfFeeType(FeeType.CLAIMISSUED);
            when(hwfService.updateHwfReferenceNumber(any(CaseData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CREATE_CLAIM_SPEC_AFTER_PAYMENT.name());
            Assertions.assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        Assertions.assertThat(handler.handledEvents()).contains(FEE_PAYMENT_OUTCOME);
    }
}
