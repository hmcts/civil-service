package uk.gov.hmcts.reform.dashboard.services;

import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

public record DraftClaimCreationResult(DraftStoreEntity draftClaim, boolean newlyCreated) {

    public static DraftClaimCreationResult newDraft(DraftStoreEntity draftClaim) {
        return new DraftClaimCreationResult(draftClaim, true);
    }

    public static DraftClaimCreationResult existingDraft(DraftStoreEntity draftClaim) {
        return new DraftClaimCreationResult(draftClaim, false);
    }
}
