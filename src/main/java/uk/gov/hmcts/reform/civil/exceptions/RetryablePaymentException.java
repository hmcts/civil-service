package uk.gov.hmcts.reform.civil.exceptions;

public class RetryablePaymentException extends PaymentsApiException {

    public RetryablePaymentException(String message) {
        super(message);
    }

    public RetryablePaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
