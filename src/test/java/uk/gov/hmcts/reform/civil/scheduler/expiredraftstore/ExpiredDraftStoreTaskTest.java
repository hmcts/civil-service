package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreTransactionService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredDraftStoreTaskTest {

    @Mock
    private DraftStoreTransactionService draftStoreTransactionService;

    private ExpiredDraftStoreTask task;

    @BeforeEach
    void setUp() {
        task = new ExpiredDraftStoreTask(draftStoreTransactionService);
    }

    @Test
    void shouldReturnBatchItemId() {
        UUID id = UUID.randomUUID();
        List<UUID> batch = List.of(id, UUID.randomUUID());

        String itemId = task.getItemId(batch);

        assertThat(itemId).isEqualTo(String.format("batch-%s-(2-items)", id));
    }

    @Test
    void shouldReturnEmptyBatchItemIdWhenNullOrEmpty() {
        assertThat(task.getItemId(null)).isEqualTo("empty-batch");
        assertThat(task.getItemId(List.of())).isEqualTo("empty-batch");
    }

    @Test
    void shouldDeleteBatchWhenNotEmpty() {
        List<UUID> batch = List.of(UUID.randomUUID());
        when(draftStoreTransactionService.deleteByIdsInNewTransaction(batch)).thenReturn(1);

        task.accept(batch);

        verify(draftStoreTransactionService).deleteByIdsInNewTransaction(batch);
    }

    @Test
    void shouldDoNothingWhenBatchIsNullOrEmpty() {
        task.accept(null);
        task.accept(List.of());

        verifyNoInteractions(draftStoreTransactionService);
    }
}
