package uk.gov.hmcts.reform.civil.enums;

import uk.gov.hmcts.reform.civil.exceptions.InvalidPaymentStatusException;

import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public enum PaymentStatus {
    SUCCESS,
    FAILED;

    private static final Map<String, PaymentStatus> LOOKUP = stream(values())
        .collect(toMap(status -> status.toString().toUpperCase(), identity()));

    public static PaymentStatus resolvePaymentStatus(String status) {
        return Optional.ofNullable(status)
            .map(String::toUpperCase)
            .map(LOOKUP::get)
            .orElseThrow(() -> new InvalidPaymentStatusException("Invalid payment status: " + status));
    }

    public static boolean isValid(String status) {
        return status != null && LOOKUP.containsKey(status.toUpperCase());
    }
}
