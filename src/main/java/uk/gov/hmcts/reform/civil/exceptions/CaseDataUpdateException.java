package uk.gov.hmcts.reform.civil.exceptions;

public class CaseDataUpdateException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Case data update failed";

    public CaseDataUpdateException() {
        super(ERROR_MESSAGE);
    }

    public CaseDataUpdateException(String message) {
        super(message);
    }

    public CaseDataUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
