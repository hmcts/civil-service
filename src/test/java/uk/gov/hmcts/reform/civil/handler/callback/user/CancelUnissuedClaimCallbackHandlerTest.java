package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CANCEL_UNISSUED_CLAIM;

@ExtendWith(MockitoExtension.class)
class CancelUnissuedClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    private CancelUnissuedClaimCallbackHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new CancelUnissuedClaimCallbackHandler(objectMapper);
    }

    private CaseData eligibleCaseData() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);
        return caseData;
    }

    private CaseData eligibleLrCaseData(CaseCategory caseCategory) {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData.setCaseAccessCategory(caseCategory);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);
        return caseData;
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_whenSpecLipClaimantOneVOnePendingCaseIssued() {
            CallbackParams params = callbackParamsOf(eligibleCaseData(), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenUnspecClaim() {
            CaseData caseData = eligibleCaseData();
            caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimCallbackHandler.EVENT_NOT_ALLOWED);
        }

        @Test
        void shouldReturnNoError_whenSpecLrClaimantOneVOnePendingCaseIssued() {
            CallbackParams params = callbackParamsOf(eligibleLrCaseData(CaseCategory.SPEC_CLAIM), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenUnspecLrClaimantOneVOnePendingCaseIssued() {
            CallbackParams params = callbackParamsOf(eligibleLrCaseData(CaseCategory.UNSPEC_CLAIM), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenLrClaimantMultiParty() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .multiPartyClaimTwoDefendantSolicitors().build();
            caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
            caseData.setApplicant1Represented(YesOrNo.YES);
            caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimCallbackHandler.EVENT_NOT_ALLOWED);
        }

        @Test
        void shouldReturnError_whenLrClaimAlreadyIssued() {
            CaseData caseData = eligibleLrCaseData(CaseCategory.UNSPEC_CLAIM);
            caseData.setCcdState(CaseState.CASE_ISSUED);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimCallbackHandler.EVENT_NOT_ALLOWED);
        }

        @Test
        void shouldReturnError_whenMultiParty() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .multiPartyClaimTwoDefendantSolicitors().build();
            caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimCallbackHandler.EVENT_NOT_ALLOWED);
        }

        @Test
        void shouldReturnError_whenClaimAlreadyIssued() {
            CaseData caseData = eligibleCaseData();
            caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimCallbackHandler.EVENT_NOT_ALLOWED);
        }
    }

    @Nested
    class MidEventValidateCancelReason {

        private static final String PAGE_ID = "validate-cancel-reason";

        private AboutToStartOrSubmitCallbackResponse validate(String reason) {
            CaseData caseData = eligibleLrCaseData(CaseCategory.UNSPEC_CLAIM);
            caseData.setCancelUnissuedClaimReason(reason);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        }

        @Test
        void shouldReturnNoError_whenReasonNotProvided() {
            assertThat(validate(null).getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenReasonIs200AllowedCharacters() {
            assertThat(validate("a".repeat(200)).getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenReasonHasStandardPunctuation() {
            assertThat(validate("Settled outside the portal: paid £100 (in full) - 'agreed' & \"signed\"; @#/$%+=?!\r\n—…")
                           .getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenReasonExceeds200Characters() {
            assertThat(validate("a".repeat(201)).getErrors())
                .containsExactly(CancelUnissuedClaimCallbackHandler.REASON_TOO_LONG);
        }

        @Test
        void shouldReturnError_whenReasonHasForbiddenCharacters() {
            assertThat(validate("Settled <script>").getErrors())
                .containsExactly(CancelUnissuedClaimCallbackHandler.REASON_INVALID_CHARACTERS);
        }

        @Test
        void shouldReturnBothErrors_whenReasonTooLongAndHasForbiddenCharacters() {
            assertThat(validate("a".repeat(200) + "<").getErrors())
                .containsExactly(
                    CancelUnissuedClaimCallbackHandler.REASON_TOO_LONG,
                    CancelUnissuedClaimCallbackHandler.REASON_INVALID_CHARACTERS
                );
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnConfirmation_whenLrClaimant() {
            CallbackParams params = callbackParamsOf(eligibleLrCaseData(CaseCategory.SPEC_CLAIM), SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).isEqualTo(CancelUnissuedClaimCallbackHandler.CONFIRMATION_HEADER);
            assertThat(response.getConfirmationBody()).isEqualTo(CancelUnissuedClaimCallbackHandler.CONFIRMATION_BODY);
        }

        @Test
        void shouldReturnEmptyConfirmation_whenLipClaimant() {
            CallbackParams params = callbackParamsOf(eligibleCaseData(), SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).isNull();
            assertThat(response.getConfirmationBody()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSetPreviousStateCancelledDateBusinessProcessAndKeepReason() {
            CaseData caseData = eligibleCaseData();
            caseData.setCancelUnissuedClaimReason("Settled outside the portal");
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getPreviousCCDState()).isEqualTo(CaseState.PENDING_CASE_ISSUED);
            assertThat(updated.getCancelUnissuedClaimReason()).isEqualTo("Settled outside the portal");
            assertThat(updated.getCancelUnissuedClaimDate()).isEqualTo(LocalDate.now());
            assertThat(updated.getBusinessProcess().getCamundaEvent()).isEqualTo(CANCEL_UNISSUED_CLAIM.name());
            assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
        }
    }

    @Test
    void handledEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).containsOnly(CANCEL_UNISSUED_CLAIM);
    }
}
