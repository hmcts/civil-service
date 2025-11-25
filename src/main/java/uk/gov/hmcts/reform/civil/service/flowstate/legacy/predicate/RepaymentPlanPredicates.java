package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

/**
 * Cohesive predicates about repayment plan decisions.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class RepaymentPlanPredicates {

    private RepaymentPlanPredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> acceptRepaymentPlan = caseData ->
        caseData.isLipvLipOneVOne()
            ? caseData.hasApplicantAcceptedRepaymentPlan() && caseData.getTakenOfflineByStaffDate() == null
            : caseData.hasApplicantAcceptedRepaymentPlan();

    public static final Predicate<CaseData> rejectRepaymentPlan = caseData ->
        caseData.isLipvLipOneVOne()
            ? caseData.hasApplicantRejectedRepaymentPlan() && caseData.getTakenOfflineByStaffDate() == null
            : caseData.hasApplicantRejectedRepaymentPlan();
}
