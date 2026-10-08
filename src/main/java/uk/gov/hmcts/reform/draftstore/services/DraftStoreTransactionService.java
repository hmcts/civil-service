package uk.gov.hmcts.reform.draftstore.services;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
        log.info("Creating draft type={} draftId={}", draft.getDraftType(), draft.getId());
        return draftStoreRepository.saveAndFlush(draft);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteInNewTransaction(DraftStoreEntity draft) {
        Objects.requireNonNull(draft, "draft must not be null");
        entityManager.joinTransaction();
        log.info("Deleting expired draft type={} draftId={}", draft.getDraftType(), draft.getId());
        draftStoreRepository.delete(draft);
        draftStoreRepository.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long deleteByIdInNewTransaction(UUID draftId, String userId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(draftType, "draftType must not be null");
        entityManager.joinTransaction();
        log.info("Deleting draft type={} draftId={}", draftType, draftId);
        long deleted = draftStoreRepository.deleteByIdAndUserIdAndDraftType(
            draftId,
            userId,
            draftType
        );
        draftStoreRepository.flush();
        return deleted;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteByIdsInNewTransaction(List<UUID> draftIds) {
        Objects.requireNonNull(draftIds, "draftIds must not be null");
        if (draftIds.isEmpty()) {
            return 0;
        }
        entityManager.joinTransaction();
        int deleted = draftStoreRepository.deleteByIds(draftIds);
        log.info("Deleted {} expired drafts", deleted);
        return deleted;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateExpiresAtInNewTransaction(UUID draftId,
                                               String userId,
                                               DraftType draftType,
                                               OffsetDateTime expiresAt,
                                               OffsetDateTime updatedAt) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(draftType, "draftType must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        entityManager.joinTransaction();
        log.info("Updating expiresAt for draft type={} draftId={}", draftType, draftId);
        return draftStoreRepository.updateExpiresAt(draftId, userId, draftType, expiresAt, updatedAt);
    }
}
