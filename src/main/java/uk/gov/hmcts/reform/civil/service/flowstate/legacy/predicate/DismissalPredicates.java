package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

/**
 * Cohesive predicates about dismissal/time-limit outcomes.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class DismissalPredicates {

    private DismissalPredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = DismissalPredicates::getPredicateForCaseDismissedAfterDetailNotified;

    private static boolean getPredicateForCaseDismissedAfterDetailNotified(CaseData caseData) {
        boolean commonConditions = caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ClaimResponseIntentionType() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getTakenOfflineByStaffDate() == null;

        MultiPartyScenario scenario = getMultiPartyScenario(caseData);

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP || scenario == MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP) {
            return commonConditions
                && caseData.getRespondent2AcknowledgeNotificationDate() == null
                && caseData.getRespondent2TimeExtensionDate() == null
                && caseData.getRespondent2ClaimResponseIntentionType() == null
                && caseData.getRespondent2ResponseDate() == null;
        }

        return commonConditions;
    }

    public static final Predicate<CaseData> pastClaimDetailsNotificationDeadline = caseData ->
        caseData.getClaimDetailsNotificationDeadline() != null
            && caseData.getClaimDetailsNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getClaimNotificationDate() != null;

    public static final Predicate<CaseData> claimDismissedByCamunda = caseData ->
        caseData.getClaimDismissedDate() != null;

    public static final Predicate<CaseData> caseDismissedPastHearingFeeDue = caseData ->
        caseData.getCaseDismissedHearingFeeDueDate() != null;
}
