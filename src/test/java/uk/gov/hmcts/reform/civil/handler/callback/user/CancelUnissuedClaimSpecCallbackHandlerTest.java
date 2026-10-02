package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CANCEL_UNISSUED_CLAIM_SPEC;

@ExtendWith(MockitoExtension.class)
class CancelUnissuedClaimSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    private CancelUnissuedClaimSpecCallbackHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new CancelUnissuedClaimSpecCallbackHandler(objectMapper);
    }

    private CaseData eligibleCaseData() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setApplicant1Represented(YesOrNo.NO);
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

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimSpecCallbackHandler.EVENT_NOT_ALLOWED);
        }

        @Test
        void shouldReturnError_whenClaimantRepresented() {
            CaseData caseData = eligibleCaseData();
            caseData.setApplicant1Represented(YesOrNo.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimSpecCallbackHandler.EVENT_NOT_ALLOWED);
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

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimSpecCallbackHandler.EVENT_NOT_ALLOWED);
        }

        @Test
        void shouldReturnError_whenClaimAlreadyIssued() {
            CaseData caseData = eligibleCaseData();
            caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(CancelUnissuedClaimSpecCallbackHandler.EVENT_NOT_ALLOWED);
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSetPreviousStateCancelledDateBusinessProcessAndKeepReason() {
            CaseData caseData = eligibleCaseData();
            caseData.setCancelUnissuedClaimSpecReason("Settled outside the portal");
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getPreviousCCDState()).isEqualTo(CaseState.PENDING_CASE_ISSUED);
            assertThat(updated.getCancelUnissuedClaimSpecReason()).isEqualTo("Settled outside the portal");
            assertThat(updated.getCancelUnissuedClaimSpecDate()).isEqualTo(LocalDate.now());
            assertThat(updated.getBusinessProcess().getCamundaEvent()).isEqualTo(CANCEL_UNISSUED_CLAIM_SPEC.name());
            assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
        }
    }

    @Test
    void handledEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).containsOnly(CANCEL_UNISSUED_CLAIM_SPEC);
    }
}
