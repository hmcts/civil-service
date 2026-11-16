package uk.gov.hmcts.reform.dashboard.exceptions;

public class DraftClaimAlreadyExistsException extends RuntimeException {

    public DraftClaimAlreadyExistsException() {
        super("An active draft claim already exists for this user");
    }

    public DraftClaimAlreadyExistsException(Throwable cause) {
        super("An active draft claim already exists for this user", cause);
    }
}
