package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface PaymentPredicate {

    @BusinessRule(
        group = "Payment",
        summary = "Pay Immediately (Part admission)",
        description = "Part admission payment time (IMMEDIATELY)"
    )
    Predicate<CaseData> payImmediatelyPartAdmission =
        CaseDataPredicate.Payment.isPayImmediately;

    @BusinessRule(
        group = "Payment",
        summary = "Payment Pay Immediately (Full admission)",
        description = "Full admission 1v1 with when to be paid and applicant not proceed"
    )
    Predicate<CaseData> payImmediatelyFullAdmission =
        CaseDataPredicate.Claim.isSpecClaim
            .and(CaseDataPredicate.MultiParty.isOneVOne)
            .and(CaseDataPredicate.Payment.hasWhenToBePaid)
            .and(CaseDataPredicate.Applicant.hasProceedDecision.negate());

    @BusinessRule(
        group = "Payment",
        summary = "Payment successful",
        description = "Applicant represented and a payment success timestamp or claim-issued success was recorded"
    )
    Predicate<CaseData> successful =
        CaseDataPredicate.Applicant.isRepresented
            .and(CaseDataPredicate.Payment.hasPaymentSuccessfulDate
                     .or(CaseDataPredicate.Payment.claimIssuedPaymentSucceeded));

    @BusinessRule(
        group = "Payment",
        summary = "Payment failed",
        description = "Applicant represented and a payment success timestamp or claim-issued failed was recorded"
    )
    Predicate<CaseData> failed =
        CaseDataPredicate.Applicant.isRepresented
            .and(CaseDataPredicate.Payment.paymentDetailsFailed
                     .or(CaseDataPredicate.Payment.claimIssuedPaymentFailed));


}
