package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DraftClaimReference;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

@Component
public class DeleteDraftTask extends MigrationTaskWithMetadata<DraftClaimReference> {

    private final DraftStoreService draftStoreService;

    public DeleteDraftTask(DraftStoreService draftStoreService) {
        super(
            DraftClaimReference.class,
            "DeleteDraftTask",
            "Delete draft migration",
            "Deletes drafts listed in the migration input"
        );
        this.draftStoreService = draftStoreService;
    }

    @Override
    protected void migrateUserDrafts(DraftClaimReference reference) {
        draftStoreService.deleteDraft(
            reference.getDraftUuid(),
            reference.getUserId(),
            DraftType.DRAFT_CLAIM
        );
    }
}
