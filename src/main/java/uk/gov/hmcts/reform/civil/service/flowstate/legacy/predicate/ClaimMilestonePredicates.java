package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

/**
 * Cohesive predicates about claim milestones (issue and claim-details time extension).
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class ClaimMilestonePredicates {

    private ClaimMilestonePredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> claimIssued = caseData ->
        caseData.getClaimNotificationDeadline() != null;

    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null;
}
