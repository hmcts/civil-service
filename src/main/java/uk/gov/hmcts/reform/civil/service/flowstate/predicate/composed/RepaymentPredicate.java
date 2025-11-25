package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class RepaymentPredicate {

    @BusinessRule(
        group = "Repayment",
        summary = "Repayment plan accepted",
        description = "Applicant has accepted the proposed repayment plan and case is not taken offline in LiP condition"
    )
    public static final Predicate<CaseData> acceptRepaymentPlan =
        CaseDataPredicate.RepaymentPlan.accepted.and(
            (CaseDataPredicate.Lip.isLiPCase.and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate()))
                .or(CaseDataPredicate.Lip.isLiPCase.negate())
        );

    @BusinessRule(
        group = "Repayment",
        summary = "Repayment plan rejected",
        description = "Applicant has rejected the proposed repayment plan and case is not taken offline in LiP condition"
    )
    public static final Predicate<CaseData> rejectRepaymentPlan =
        CaseDataPredicate.RepaymentPlan.rejected.and(
            (CaseDataPredicate.Lip.isLiPCase.and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate()))
                .or(CaseDataPredicate.Lip.isLiPCase.negate())
        );

    private RepaymentPredicate() {
    }
}
