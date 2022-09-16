package uk.gov.hmcts.reform.civil.service.pininpost.exception;

public class PinNotMatchException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Pin does not match";

    public PinNotMatchException() {
        super(ERROR_MESSAGE);
    }
}
