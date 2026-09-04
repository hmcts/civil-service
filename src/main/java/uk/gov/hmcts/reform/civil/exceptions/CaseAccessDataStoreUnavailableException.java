package uk.gov.hmcts.reform.civil.exceptions;

public class CaseAccessDataStoreUnavailableException extends RuntimeException {

    public CaseAccessDataStoreUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
