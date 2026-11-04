package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DraftClaimReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;


@Component
public class DeleteDraftTask extends MigrationTask<DraftClaimReference> {

    private final DraftStoreService draftStoreService;

    public DeleteDraftTask(DraftStoreService draftStoreService) {
        super(DraftClaimReference.class);
        this.draftStoreService = draftStoreService;
    }

    @Override
    protected void migrateUserDrafts(DraftClaimReference reference) {
        draftStoreService.deleteDraft(
            java.util.UUID.fromString(reference.getCaseReference()),
            reference.getUserId(),
            DraftType.DRAFT_CLAIM
        );
    }

    @Override
    protected String getTaskName() {
        return "DeleteDraftTask";
    }

    @Override
    protected String getEventSummary() {
        return "Delete draft migration";
    }

    @Override
    protected String getEventDescription() {
        return "Deletes drafts listed in the migration input";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, DraftClaimReference caseReference) {
        return caseData;
    }
}
