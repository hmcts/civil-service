package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class ClaimPredicate {

    @BusinessRule(
        group = "Claim",
        summary = "Claim issued",
        description = "Claim has a claim notification deadline (claim has been issued/notified)"
    )
    public static final Predicate<CaseData> issued = CaseDataPredicate.Claim.hasNotificationDeadline;

    @BusinessRule(
        group = "Claim",
        summary = "Spec claim",
        description = "Case is a SPEC (damages) claim as per case access category"
    )
    public static final Predicate<CaseData> isSpec = CaseDataPredicate.Claim.isSpecClaim;

    private ClaimPredicate() {
    }
}
