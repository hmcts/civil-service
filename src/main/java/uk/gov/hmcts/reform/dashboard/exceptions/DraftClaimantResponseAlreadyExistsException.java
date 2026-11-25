package uk.gov.hmcts.reform.dashboard.exceptions;

public class DraftClaimantResponseAlreadyExistsException extends RuntimeException {

    public DraftClaimantResponseAlreadyExistsException(Throwable cause) {
        super("An active draft claimant response already exists for this user and case", cause);
    }
}
