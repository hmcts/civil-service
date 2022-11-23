package uk.gov.hmcts.reform.civil.exceptions;

public class CaseIdNotProvidedException extends NotRetryableException {

    private static final String ERROR_MESSAGE = "The caseId was not provided";

    public CaseIdNotProvidedException() {
        super(ERROR_MESSAGE);
    }

    public CaseIdNotProvidedException(String message) {
        super(message);
    }

    public CaseIdNotProvidedException(String message, Throwable cause) {
        super(message, cause);
    }
}
