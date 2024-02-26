package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FEE_PAYMENT_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.FeePaymentOutcomeHWFCallBackHandler.WRONG_REMISSION_TYPE_SELECTED;

@ExtendWith(MockitoExtension.class)
public class FeePaymentOutcomeHWFCallBackHandlerTest extends BaseCallbackHandlerTest {

    private FeePaymentOutcomeHWFCallBackHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        HWFFeePaymentOutcomeService hwfService = new HWFFeePaymentOutcomeService();
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
        void shouldUpdateTheDataBaseWithHWFRefNumber_WhenFeeTye_ClaimIssue() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseDataLip(CaseDataLiP.builder()
                                 .helpWithFees(HelpWithFees.builder()
                                                   .helpWithFee(YesOrNo.NO).build()).build())
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                                              .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                              .hwfFullRemissionGrantedForClaimIssue(YesOrNo.YES).build())
                .build();

            caseData.toBuilder().hwfFeeType(FeeType.CLAIMISSUED);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getCaseDataLiP().getHelpWithFees().getHelpWithFeesReferenceNumber()).isEqualTo("HWF-1C4-E34");
        }

        @Test
        void shouldUpdateTheDataBaseWithHWFRefNumber_WhenFeeType_Hearing() {
            CaseData caseData = CaseData.builder()
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                                              .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                              .hwfFullRemissionGrantedForHearingFee(YesOrNo.YES).build())
                .hwfFeeType(FeeType.HEARING)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getHearingHelpFeesReferenceNumber()).isEqualTo("HWF-1C4-E34");
        }

        @Test
        void shouldValidateRemissionTypeForHearingFee() {
            CaseData caseData = CaseData.builder()
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                                              .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                              .hwfFullRemissionGrantedForHearingFee(YesOrNo.YES).build())
                .hwfFeeType(FeeType.HEARING)
                .hearingHwfDetails(HelpWithFeesDetails.builder()
                                         .outstandingFeeInPounds(BigDecimal.valueOf(100.00))
                                         .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-type");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(WRONG_REMISSION_TYPE_SELECTED);
        }

        @Test
        void shouldValidateRemissionTypeForClaimIssFee() {
            CaseData caseData = CaseData.builder()
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                                              .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                              .hwfFullRemissionGrantedForClaimIssue(YesOrNo.YES).build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .outstandingFeeInPounds(BigDecimal.valueOf(100.00))
                                           .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-type");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(WRONG_REMISSION_TYPE_SELECTED);
        }

        @Test
        void shouldUpdateBusinessProcess_WhenFeeType_ClaimIssue() {
            CaseData caseData = CaseData.builder()
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();

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
