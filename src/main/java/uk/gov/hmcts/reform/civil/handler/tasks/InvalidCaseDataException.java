package uk.gov.hmcts.reform.civil.handler.tasks;

public class InvalidCaseDataException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Invalid case data";

    public InvalidCaseDataException() {
        super(ERROR_MESSAGE);
    }

    public InvalidCaseDataException(String message) {
        super(message);
    }

    public InvalidCaseDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
