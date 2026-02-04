package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface PaymentPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Payment",
        summary = "Pay Immediately (Part admission)",
        description = "Part admission where the payment time selected is 'IMMEDIATELY'"
    )
    Predicate<CaseData> payImmediatelyPartAdmit =
        CaseDataPredicate.Payment.isPayImmediately;

    @BusinessRule(
        group = "Payment",
        summary = "Payment Pay Immediately Accepted",
        description = "Part admission payment time (IMMEDIATELY) Accepted"
    )
    Predicate<CaseData> payImmediatelyAcceptedPartAdmit =
        CaseDataPredicate.Payment.isPartAdmitPayImmediately;

    @BusinessRule(
        group = "Payment",
        summary = "Pay immediately (Full admission)",
        description = "SPEC 1v1 full admission where 'when to be paid' is set and the applicant chose not to proceed"
    )
    Predicate<CaseData> payImmediatelyFullAdmission =
        CaseDataPredicate.Claim.isSpecClaim
            .and(CaseDataPredicate.MultiParty.isOneVOne)
            .and(CaseDataPredicate.Payment.hasWhenToBePaid)
            .and(CaseDataPredicate.Applicant.hasProceedDecision.negate());

    @BusinessRule(
        group = "Payment",
        summary = "Payment successful",
        description = "Card payment for the issue fee succeeded (or claim issue recorded as successful) and the applicant is represented"
    )
    Predicate<CaseData> successful =
        CaseDataPredicate.Applicant.isRepresented
            .and(CaseDataPredicate.Payment.hasPaymentSuccessfulDate
                     .or(CaseDataPredicate.Payment.claimIssuedPaymentSucceeded));

    @BusinessRule(
        group = "Payment",
        summary = "Payment failed",
        description = "Card payment for the issue fee failed (or claim issue recorded as failed) and the applicant is represented"
    )
    Predicate<CaseData> failed =
        CaseDataPredicate.Applicant.isRepresented
            .and(CaseDataPredicate.Payment.paymentDetailsFailed
                     .or(CaseDataPredicate.Payment.claimIssuedPaymentFailed));


}
