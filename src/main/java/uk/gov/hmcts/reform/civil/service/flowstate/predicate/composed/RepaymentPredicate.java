package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface RepaymentPredicate {

    @BusinessRule(
        group = "Repayment",
        summary = "Repayment plan accepted",
        description = "Applicant has accepted the proposed repayment plan and case is not taken offline in LiP condition"
    )
    Predicate<CaseData> acceptRepaymentPlan =
        CaseDataPredicate.RepaymentPlan.accepted.and(
            (CaseDataPredicate.Lip.isLiPvLipCase.and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate()))
                .or(CaseDataPredicate.Lip.isLiPvLipCase.negate())
        );

    @BusinessRule(
        group = "Repayment",
        summary = "Repayment plan rejected",
        description = "Applicant has rejected the proposed repayment plan and case is not taken offline in LiP condition"
    )
    Predicate<CaseData> rejectRepaymentPlan =
        CaseDataPredicate.RepaymentPlan.rejected.and(
            (CaseDataPredicate.Lip.isLiPvLipCase.and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate()))
                .or(CaseDataPredicate.Lip.isLiPvLipCase.negate())
        );


}
