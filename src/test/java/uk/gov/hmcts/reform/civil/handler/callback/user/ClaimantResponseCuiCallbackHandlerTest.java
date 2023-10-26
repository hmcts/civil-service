package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)

@SpringBootTest(classes = {
    ClaimantResponseCuiCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class ClaimantResponseCuiCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ClaimantResponseCuiCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdateBusinessProcess() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(
                    CaseDataLiP.builder()
                        .applicant1ClaimMediationSpecRequiredLip(
                            ClaimantMediationLip.builder()
                                .hasAgreedFreeMediation(MediationDecision.Yes)
                                .build())
                        .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CLAIMANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

        @Test
        void shouldOnlyUpdateClaimStatus_whenPartAdmitNotSettled_NoMediation() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .applicant1AcceptAdmitAmountPaidSpec(NO)
                .caseDataLip(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder().hasAgreedFreeMediation(
                        MediationDecision.No).build())
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CLAIMANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

        }

        @Test
        void shouldUpdateCaseStateToJudicialReferral_WhenPartAdmitNoSettle_NoMediation() {
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                             .hasAgreedFreeMediation(MediationDecision.No).build())
                .build();
            CaseData caseData =
                CaseDataBuilder.builder().caseDataLip(caseDataLiP).applicant1AcceptAdmitAmountPaidSpec(NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response.getState());

        }

        @Test
        void shouldUpdateCaseStateToJudicialReferral_WhenNotReceivedPayment_NoMediation_ForPartAdmit() {
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                             .hasAgreedFreeMediation(MediationDecision.No).build())
                .build();
            CaseData caseData =
                CaseDataBuilder.builder().caseDataLip(caseDataLiP).applicant1PartAdmitConfirmAmountPaidSpec(NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response.getState());

        }

        @Test
        void shouldUpdateCaseStateToJudicialReferral_WhenFullDefence_NotPaid_NoMediation() {
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                             .hasAgreedFreeMediation(MediationDecision.No).build())
                .build();
            CaseData caseData =
                CaseDataBuilder.builder().caseDataLip(caseDataLiP).applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response.getState());

        }

        @Test
        void shouldUpdateCaseStateToJudicialReferral_WhenFullDefence() {
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                             .hasAgreedFreeMediation(MediationDecision.No).build())
                .build();
            CaseData caseData =
                CaseDataBuilder.builder().caseDataLip(caseDataLiP).applicant1ProceedWithClaim(YES)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response.getState());

        }
    }
}
