package uk.gov.hmcts.reform.dashboard.exceptions;

import java.util.UUID;

public class DraftClaimantResponseNotFoundException extends RuntimeException {

    public DraftClaimantResponseNotFoundException(UUID draftId) {
        super("No active draft claimant response found for draftId " + draftId);
    }

    public DraftClaimantResponseNotFoundException(String caseId) {
        super("No active draft claimant response found for caseId " + caseId);
    }
}
