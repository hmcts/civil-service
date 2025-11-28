package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface RepaymentPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Repayment",
        summary = "Repayment plan accepted",
        description = "Applicant accepted the proposed repayment plan; if LiP v LiP, the case has not been taken offline by staff"
    )
    Predicate<CaseData> acceptRepaymentPlan =
        CaseDataPredicate.RepaymentPlan.accepted.and(
            (CaseDataPredicate.Lip.isLiPvLipCase.and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate()))
                .or(CaseDataPredicate.Lip.isLiPvLipCase.negate())
        );

    @BusinessRule(
        group = "Repayment",
        summary = "Repayment plan rejected",
        description = "Applicant rejected the proposed repayment plan; if LiP v LiP, the case has not been taken offline by staff"
    )
    Predicate<CaseData> rejectRepaymentPlan =
        CaseDataPredicate.RepaymentPlan.rejected.and(
            (CaseDataPredicate.Lip.isLiPvLipCase.and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate()))
                .or(CaseDataPredicate.Lip.isLiPvLipCase.negate())
        );


}
