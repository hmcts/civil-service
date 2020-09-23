package uk.gov.hmcts.reform.unspec.service.flowstate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimantConfirmService;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimantIssueClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimantRespondToDefence;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimantRespondToRequestForExtension;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.defendantAcknowledgeService;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.defendantAskForAnExtension;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.defendantRespondToClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.schedulerStayClaim;

class FlowPredicateTest {

    @Nested
    class ClaimIssuedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedSate() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            assertTrue(claimantIssueClaim.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftSate() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimantIssueClaim.test(caseData));
        }
    }

    @Nested
    class ConfirmedServicePredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtConfirmedService() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
            assertTrue(claimantConfirmService.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtClaimIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            assertFalse(claimantConfirmService.test(caseData));
        }
    }

    @Nested
    class DefendantAcknowledgedServicePredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateServiceAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceAcknowledge().build();
            assertTrue(defendantAcknowledgeService.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimCreated() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            assertFalse(defendantAcknowledgeService.test(caseData));
        }
    }

    @Nested
    class DefendantRespondToClaimPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateRespondedToClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();
            assertTrue(defendantRespondToClaim.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceConfirmed() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
            assertFalse(defendantRespondToClaim.test(caseData));
        }
    }

    @Nested
    class DefendantAskForAnExtensionPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateExtensionRequested() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionRequested().build();
            assertTrue(defendantAskForAnExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceAcknowledge().build();
            assertFalse(defendantAskForAnExtension.test(caseData));
        }
    }

    @Nested
    class ClaimantRespondToRequestForExtensionPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateExtensionResponded() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionResponded().build();
            assertTrue(claimantRespondToRequestForExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionRequested().build();
            assertFalse(claimantRespondToRequestForExtension.test(caseData));
        }
    }

    @Nested
    class ClaimantRespondToDefencePredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateFullDefence().build();
            assertTrue(claimantRespondToDefence.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();
            assertFalse(claimantRespondToDefence.test(caseData));
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
}
