package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

/**
 * Cohesive predicates about payments.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class PaymentPredicates {

    private PaymentPredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> paymentSuccessful = caseData ->
        !caseData.isApplicantNotRepresented()
            && (caseData.getPaymentSuccessfulDate() != null
            || (caseData.getClaimIssuedPaymentDetails() != null
            && caseData.getClaimIssuedPaymentDetails().getStatus() == SUCCESS));
}
