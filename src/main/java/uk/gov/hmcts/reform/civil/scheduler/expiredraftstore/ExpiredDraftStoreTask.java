package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredDraftStoreTask implements ScheduledTask<List<UUID>, String> {

    private final DraftStoreRepository draftStoreRepository;

    @Override
    public String getItemId(List<UUID> batch) {
        if (batch == null || batch.isEmpty()) {
            return "empty-batch";
        }
        return String.format("batch-%s-(%d-items)", batch.getFirst(), batch.size());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void accept(List<UUID> batch) {
        if (batch != null && !batch.isEmpty()) {
            draftStoreRepository.deleteByIds(batch);
        }
    }
}
