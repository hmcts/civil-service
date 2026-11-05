package uk.gov.hmcts.reform.dashboard.exceptions;

public class DraftDefendantResponseAlreadyExistsException extends RuntimeException {

    public DraftDefendantResponseAlreadyExistsException(Throwable cause) {
        super("An active draft defendant response already exists for this user", cause);
    }
}
