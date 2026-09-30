package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExpiredDraftStoreTask {

    private final DraftStoreRepository draftStoreRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteExpiredBatch(OffsetDateTime now, Pageable pageRequest) {
        List<UUID> expiredIds = draftStoreRepository.findExpiredIds(now, pageRequest);
        if (expiredIds.isEmpty()) {
            return 0;
        }
        return draftStoreRepository.deleteByIds(expiredIds);
    }
}
