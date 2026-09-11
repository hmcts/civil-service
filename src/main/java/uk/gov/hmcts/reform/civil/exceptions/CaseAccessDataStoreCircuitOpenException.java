package uk.gov.hmcts.reform.civil.exceptions;

public class CaseAccessDataStoreCircuitOpenException extends CaseAccessDataStoreUnavailableException {

    public CaseAccessDataStoreCircuitOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
