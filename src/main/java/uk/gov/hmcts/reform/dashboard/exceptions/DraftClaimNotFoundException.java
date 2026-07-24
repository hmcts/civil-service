package uk.gov.hmcts.reform.dashboard.exceptions;

import java.util.UUID;

public class DraftClaimNotFoundException extends RuntimeException {

    public DraftClaimNotFoundException() {
        super("No active draft claim found");
    }

    public DraftClaimNotFoundException(UUID draftId) {
        super("No active draft claim found for draftId " + draftId);
    }
}
