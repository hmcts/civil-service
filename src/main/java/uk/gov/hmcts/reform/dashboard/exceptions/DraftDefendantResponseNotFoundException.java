package uk.gov.hmcts.reform.dashboard.exceptions;

import java.util.UUID;

public class DraftDefendantResponseNotFoundException extends RuntimeException {

    public DraftDefendantResponseNotFoundException(UUID draftId) {
        super("No active draft defendant response found for draftId " + draftId);
    }

    public DraftDefendantResponseNotFoundException(String caseId) {
        super("No active draft defendant response found for caseId " + caseId);
    }
}
