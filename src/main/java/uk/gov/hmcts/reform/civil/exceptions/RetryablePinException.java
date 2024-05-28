package uk.gov.hmcts.reform.civil.exceptions;

public class RetryablePinException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Unauthorized, Pin retry";

    public RetryablePinException() {
        super(ERROR_MESSAGE);
    }
}
