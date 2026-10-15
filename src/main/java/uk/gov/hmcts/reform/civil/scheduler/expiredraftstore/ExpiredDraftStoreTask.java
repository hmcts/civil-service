package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreTransactionService;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredDraftStoreTask implements ScheduledTask<List<UUID>, String> {

    private final DraftStoreTransactionService draftStoreTransactionService;

    @Override
    public String getItemId(List<UUID> batch) {
        if (batch == null || batch.isEmpty()) {
            return "empty-batch";
        }
        return String.format("batch-%s-(%d-items)", batch.getFirst(), batch.size());
    }

    @Override
    public void accept(List<UUID> batch) {
        if (batch != null && !batch.isEmpty()) {
            draftStoreTransactionService.deleteByIdsInNewTransaction(batch);
        }
    }
}
