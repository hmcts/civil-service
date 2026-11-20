package uk.gov.hmcts.reform.dashboard.exceptions;

public class DraftGeneralApplicationAlreadyExistsException extends RuntimeException {

    public DraftGeneralApplicationAlreadyExistsException(String reference) {
        super("An active general application draft already exists for " + reference);
    }

    public DraftGeneralApplicationAlreadyExistsException(String reference, Throwable cause) {
        super("An active general application draft already exists for " + reference, cause);
    }
}
