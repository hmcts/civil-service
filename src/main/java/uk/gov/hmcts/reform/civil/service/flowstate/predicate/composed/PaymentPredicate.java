package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class PaymentPredicate {

    @BusinessRule(
        group = "Payment",
        summary = "Payment successful",
        description = "Applicant represented and a payment success timestamp or claim-issued success was recorded"
    )
    public static final Predicate<CaseData> successful =
        CaseDataPredicate.Applicant.isRepresented
            .and(CaseDataPredicate.Payment.hasPaymentSuccessfulDate
                     .or(CaseDataPredicate.Payment.claimIssuedPaymentSucceeded));

    private PaymentPredicate() {
    }

}
