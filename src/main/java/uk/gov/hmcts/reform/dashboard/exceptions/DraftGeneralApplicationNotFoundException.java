package uk.gov.hmcts.reform.dashboard.exceptions;

import java.util.UUID;

public class DraftGeneralApplicationNotFoundException extends RuntimeException {

    public DraftGeneralApplicationNotFoundException(UUID draftId) {
        super("No active general application draft found for draftId " + draftId);
    }

    public DraftGeneralApplicationNotFoundException(String reference) {
        super("No active general application draft found for " + reference);
    }
}
