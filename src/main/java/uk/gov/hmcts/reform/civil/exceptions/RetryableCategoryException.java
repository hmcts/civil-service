package uk.gov.hmcts.reform.civil.exceptions;

public class RetryableCategoryException extends PaymentsApiException {

    public RetryableCategoryException(String message) {
        super(message);
    }

    public RetryableCategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
