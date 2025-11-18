package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

/**
 * Cohesive predicates about case categories.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class CaseCategoryPredicates {

    private CaseCategoryPredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> specClaim = caseData ->
        SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
}
