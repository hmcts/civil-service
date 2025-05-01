package uk.gov.hmcts.reform.civil.exceptions;

public class NotEarlyAdopterCourtException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Only early adopter courts are allowed to complete this action";

    public NotEarlyAdopterCourtException() {
        super(ERROR_MESSAGE);
    }
}
