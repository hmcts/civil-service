package uk.gov.hmcts.reform.unspec.service.flowstate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.applicantConfirmService;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.applicantRespondToDefence;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.applicantRespondToRequestForExtension;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimDiscontinued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimWithdrawn;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.pendingCaseIssued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentAcknowledgeService;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentAskForAnExtension;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentRespondToClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.schedulerStayClaim;

class FlowPredicateTest {

    @Nested
    class ClaimIssuedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedSate() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingCaseIssued().build();
            assertTrue(pendingCaseIssued.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftSate() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(pendingCaseIssued.test(caseData));
        }
    }

    @Nested
    class PaymentFailedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedSate() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentFailed().build();
            assertTrue(paymentFailed.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftSate() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingCaseIssued().build();
            assertFalse(paymentFailed.test(caseData));
        }
    }

    @Nested
    class PaymentSuccessfulPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedSate() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertTrue(paymentSuccessful.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftSate() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingCaseIssued().build();
            assertFalse(paymentSuccessful.test(caseData));
        }
    }

    @Nested
    class CcdStateCreatedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedSate() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            assertTrue(claimIssued.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftSate() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertFalse(claimIssued.test(caseData));
        }
    }

    @Nested
    class ConfirmedServicePredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtConfirmedService() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
            assertTrue(applicantConfirmService.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtClaimIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            assertFalse(applicantConfirmService.test(caseData));
        }
    }

    @Nested
    class RespondentAcknowledgedServicePredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateServiceAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceAcknowledge().build();
            assertTrue(respondentAcknowledgeService.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimCreated() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            assertFalse(respondentAcknowledgeService.test(caseData));
        }
    }

    @Nested
    class RespondentRespondToClaimPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateRespondedToClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();
            assertTrue(respondentRespondToClaim.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceConfirmed() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
            assertFalse(respondentRespondToClaim.test(caseData));
        }
    }

    @Nested
    class RespondentAskForAnExtensionPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateExtensionRequested() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionRequested().build();
            assertTrue(respondentAskForAnExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceAcknowledge().build();
            assertFalse(respondentAskForAnExtension.test(caseData));
        }
    }

    @Nested
    class ApplicantRespondToRequestForExtensionPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateExtensionResponded() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionResponded().build();
            assertTrue(applicantRespondToRequestForExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionRequested().build();
            assertFalse(applicantRespondToRequestForExtension.test(caseData));
        }
    }

    @Nested
    class ApplicantRespondToDefencePredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateFullDefence().build();
            assertTrue(applicantRespondToDefence.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();
            assertFalse(applicantRespondToDefence.test(caseData));
        }
    }

    @Nested
    class SchedulerStayClaimPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimStayed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimStayed().build();
            assertTrue(schedulerStayClaim.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateFullDefence().build();
            assertFalse(schedulerStayClaim.test(caseData));
        }
    }

    @Nested
    class WithdrawnClaimPredicate {

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class)
        void shouldReturnTrue_whenCaseDataAtStateClaimWithdrawn(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().withdrawClaimFrom(flowState).build();
            assertTrue(claimWithdrawn.test(caseData));
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class, mode = EnumSource.Mode.EXCLUDE, names = {"CLAIM_WITHDRAWN"})
        void shouldReturnFalse_whenCaseDataIsNotAtStateClaimWithdrawn(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().atState(flowState).build();
            assertFalse(claimWithdrawn.test(caseData));
        }
    }

    @Nested
    class DiscontinuedClaimPredicate {

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class)
        void shouldReturnTrue_whenCaseDataAtStateClaimDiscontinued(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().discontinueClaimFrom(flowState).build();
            assertTrue(claimDiscontinued.test(caseData));
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class, mode = EnumSource.Mode.EXCLUDE, names = {"CLAIM_DISCONTINUED"})
        void shouldReturnFalse_whenCaseDataIsNotAtStateClaimDiscontinued(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().atState(flowState).build();
            assertFalse(claimDiscontinued.test(caseData));
        }
    }
}
