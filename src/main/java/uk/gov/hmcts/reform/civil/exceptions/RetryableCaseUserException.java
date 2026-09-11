package uk.gov.hmcts.reform.civil.exceptions;

public class RetryableCaseUserException extends RuntimeException {

    public RetryableCaseUserException(String message) {
        super(message);
    }

    public RetryableCaseUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
