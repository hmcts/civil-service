package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;

@ExtendWith(MockitoExtension.class)
class SettleClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private SettleClaimCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturn_error_when_case_in_all_final_orders_issued_state() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().get(0)).isEqualTo("This action is not currently allowed at this stage");
        }

        @Test
        void shouldReturn_error_when_case_in_any_other_state_than_all_final_orders_issued_state() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturn_error_when_claim_is_1v1_LiPvLIP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1UnrepresentedDefendantSpec()
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().get(0)).isEqualTo("This action is not available for this claim");
        }

        @Test
        void shouldReturn_error_when_claim_is_1v2_LiPvLiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssuedUnrepresentedDefendants()
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().get(0)).isEqualTo("This action is not available for this claim");
        }

        @Test
        void shouldReturn_error_when_claim_is_1v2_LRvLiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2UnrepresentedDefendant()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().get(0)).isEqualTo("This action is not available for this claim");
        }

        @Test
        void shouldReturn_error_when_claim_is_1v2_LiPvLR() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndSameRepresentative()
                .respondent2(PartyBuilder.builder().individual().build().toBuilder().build())
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().get(0)).isEqualTo("This action is not available for this claim");
        }

        @Test
        void shouldReturn_error_when_claim_is_1v2_LRvLRvLiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().get(0)).isEqualTo("This action is not available for this claim");
        }

        @Test
        void shouldReturn_error_when_claim_is_1v2_LRvLiPvLR() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented()
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().get(0)).isEqualTo("This action is not available for this claim");
        }

        @Test
        void shouldReturn_error_when_claim_is_2v1_LiPvLiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentUnrepresented()
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().get(0)).isEqualTo("This action is not available for this claim");
        }

        @Test
        void shouldReturn_error_when_claim_is_1v1_LRvLR() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1UnrepresentedDefendantSpec()
                .respondent1Represented(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturn_error_when_claim_is_1v1_LRvLiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1UnrepresentedDefendantSpec()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturn_error_when_claim_is_1v2_LRvLR() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndSameRepresentative()
                .respondent2(PartyBuilder.builder().individual().build().toBuilder().build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturn_error_when_claim_is_2v1_LRvLR() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentUnrepresented()
                .respondent1Represented(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturn_error_when_claim_is_2v1_LRvLiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentUnrepresented()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }
    }
}
