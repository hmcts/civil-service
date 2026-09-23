package uk.gov.hmcts.reform.draftstore.services;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;
import java.util.UUID;

import java.util.Objects;

@Service
@Slf4j
public class DraftStoreTransactionService {

    private final DraftStoreRepository draftStoreRepository;
    private final EntityManager entityManager;

    public DraftStoreTransactionService(DraftStoreRepository draftStoreRepository,
                                        EntityManager entityManager) {
        this.draftStoreRepository = draftStoreRepository;
        this.entityManager = entityManager;
    }

    /**
     * Inserts the draft and flushes in a new, independent transaction so that a unique-constraint
     * violation from a concurrent request surfaces here as a {@code DataIntegrityViolationException}
     * and only rolls back this transaction, leaving the caller free to re-read the winning draft.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DraftStoreEntity saveInNewTransaction(DraftStoreEntity draft) {
        Objects.requireNonNull(draft, "draft must not be null");
        entityManager.joinTransaction();
        log.info("Creating draft typeId={} draftId={}", draft.getDraftTypeId(), draft.getId());
        return draftStoreRepository.saveAndFlush(draft);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteInNewTransaction(DraftStoreEntity draft) {
        Objects.requireNonNull(draft, "draft must not be null");
        entityManager.joinTransaction();
        log.info("Deleting expired draft typeId={} draftId={}", draft.getDraftTypeId(), draft.getId());
        draftStoreRepository.delete(draft);
        draftStoreRepository.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long deleteByIdInNewTransaction(UUID draftId, String userId, Integer draftTypeId) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(draftTypeId, "draftTypeId must not be null");
        entityManager.joinTransaction();
        log.info("Deleting draft typeId={} draftId={}", draftTypeId, draftId);
        long deleted = draftStoreRepository.deleteByIdAndUserIdAndDraftTypeId(
            draftId,
            userId,
            draftTypeId
        );
        draftStoreRepository.flush();
        return deleted;
    }
}
