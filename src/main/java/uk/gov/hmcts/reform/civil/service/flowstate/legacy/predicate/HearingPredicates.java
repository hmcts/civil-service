package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;

/**
 * Cohesive predicates about hearing readiness.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class HearingPredicates {

    private HearingPredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> isInHearingReadiness = caseData ->
        caseData.getHearingReferenceNumber() != null
            && LISTING.equals(caseData.getListingOrRelisting())
            && caseData.getCaseDismissedHearingFeeDueDate() == null
            && caseData.getTakenOfflineDate() == null
            && caseData.getTakenOfflineByStaffDate() == null;
}
