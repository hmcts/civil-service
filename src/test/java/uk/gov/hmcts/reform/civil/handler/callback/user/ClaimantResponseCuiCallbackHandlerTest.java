package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.ORGANISATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ClaimantResponseCuiCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ResponseOneVOneShowTagService.class,
    JudgementService.class
})
class ClaimantResponseCuiCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ClaimantResponseCuiCallbackHandler handler;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @MockBean
    private ResponseOneVOneShowTagService responseOneVOneShowTagService;
    @MockBean
    FeatureToggleService featureToggleService;
    @Autowired
    private JudgementService judgementService;

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
        void shouldChangeCaseState_whenApplicantRejectClaimSettlementAndAgreeToMediation() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .caseDataLip(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder().hasAgreedFreeMediation(
                    MediationDecision.Yes).build())
                            .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.IN_MEDIATION.name());
        }

        @Test
        void shouldChangeCaseState_whenApplicantRejectRepaymentPlanAndIsCompany_toAllFinalOrdersIssued() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1AcceptPartAdmitPaymentPlanSpec(NO)
                .respondent1(Party.builder()
                                   .type(COMPANY)
                                   .companyName("Test Inc")
                                   .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldChangeCaseState_whenApplicantRejectRepaymentPlanAndIsOrganisation_toAllFinalOrdersIssued() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1AcceptPartAdmitPaymentPlanSpec(NO)
                .respondent1(Party.builder()
                                 .type(ORGANISATION)
                                 .companyName("Test Inc")
                                 .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldUpdateCCJRequestPaymentDetails() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(600.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(
                    CaseDataLiP.builder()
                        .applicant1LiPResponse(ClaimantLiPResponse.builder().applicant1ChoosesHowToProceed(
                            ChooseHowToProceed.REQUEST_A_CCJ).build())
                        .build())
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(NO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .ccjPaymentDetails(ccjPaymentDetails)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CCJPaymentDetails ccjResponseForJudgement =
                getCaseData(response).getCcjPaymentDetails();
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CLAIMANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
            assertThat(ccjPaymentDetails.getCcjPaymentPaidSomeOption()).isEqualTo(ccjResponseForJudgement.getCcjPaymentPaidSomeOption());
            assertThat(caseData.getTotalClaimAmount()).isEqualTo(ccjResponseForJudgement.getCcjJudgmentAmountClaimAmount());
        }
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return mapper.convertValue(response.getData(), CaseData.class);
    }
}
