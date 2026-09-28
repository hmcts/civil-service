package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.util.Objects;

public class PaymentUtils {

    private PaymentUtils() {
        // Utility class
    }

    public static boolean isPaymentAlreadyApplied(PaymentDetails intendedPayment, PaymentDetails freshPayment) {
        return freshPayment != null && PaymentStatus.SUCCESS.equals(freshPayment.getStatus())
            && freshPayment.getReference() != null
            && intendedPayment != null && Objects.equals(intendedPayment.getReference(), freshPayment.getReference());
    }
}
