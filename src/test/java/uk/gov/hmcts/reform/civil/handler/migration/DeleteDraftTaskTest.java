package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DraftClaimReference;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteDraftTaskTest {

    @Mock
    private DraftStoreService draftStoreService;

    private DeleteDraftTask task;

    @BeforeEach
    void setUp() {
        task = new DeleteDraftTask(draftStoreService);
    }

    @Test
    void shouldDeleteDraftUsingReferenceDetails() {
        UUID draftId = UUID.randomUUID();
        DraftClaimReference reference = new DraftClaimReference();
        reference.setDraftId(draftId.toString());
        reference.setUserId("user-1");

        task.migrateUserDrafts(reference);

        verify(draftStoreService).deleteDraft(draftId, "user-1", DraftType.DRAFT_CLAIM);
    }

    @Test
    void shouldExposeTaskMetadata() {
        assertThat(task.getTaskName()).isEqualTo("DeleteDraftTask");
        assertThat(task.getEventSummary()).isEqualTo("Delete draft migration");
        assertThat(task.getEventDescription()).isEqualTo("Deletes drafts listed in the migration input");
        assertThat(task.getType()).isEqualTo(DraftClaimReference.class);
    }

    @Test
    void shouldLeaveCaseDataUnchanged() {
        DraftClaimReference reference = new DraftClaimReference();

        assertThat(task.migrateCaseData(null, reference)).isNull();
    }
}
