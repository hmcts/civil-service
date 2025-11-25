package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

/**
 * Cohesive predicates related to response flags.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class ResponseFlagPredicates {

    private ResponseFlagPredicates() {
        // Utility class
    }

    // This predicate is used in LR ITP to prevent going another path in preview
    public static final Predicate<CaseData> isOneVOneResponseFlagSpec = caseData ->
        caseData.getShowResponseOneVOneFlag() != null;
}
