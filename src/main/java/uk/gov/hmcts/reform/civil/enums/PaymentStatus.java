package uk.gov.hmcts.reform.civil.enums;

import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum PaymentStatus {
    @CCD(label = "Success")
    SUCCESS,
    @CCD(label = "Failed")
    FAILED;

    private static final Map<String, PaymentStatus> LOOKUP = stream(values())
        .collect(toMap(status -> status.toString().toUpperCase(), identity()));

    public static PaymentStatus resolvePaymentStatus(String status) {
        return Optional.ofNullable(status)
            .map(String::toUpperCase)
            .map(LOOKUP::get)
            .orElseThrow(() -> new IllegalArgumentException("Invalid payment status: " + status));
    }

    public static boolean isValid(String status) {
        return status != null && LOOKUP.containsKey(status.toUpperCase());
    }
}
