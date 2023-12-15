package uk.gov.hmcts.reform.civil.exceptions;

public class PaymentsApiException extends RuntimeException {

    public PaymentsApiException(String message) {
        super(message);
    }

    public PaymentsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
