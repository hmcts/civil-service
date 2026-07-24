package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimNotFoundException;
import uk.gov.hmcts.reform.dashboard.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class DraftStoreService {

    private static final int DRAFT_CLAIM_TYPE_ID = 1;
    private static final long DRAFT_EXPIRY_DAYS = 180;
    private static final String USER_ID_NOT_NULL = "userId must not be null";

    private final DraftStoreRepository draftStoreRepository;

    @Autowired
    public DraftStoreService(DraftStoreRepository draftStoreRepository) {
        this.draftStoreRepository = draftStoreRepository;
    }

    public DraftStoreEntity createDraftClaim(String userId, String caseId, Map<String, Object> payload) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        DraftStoreEntity draftStoreEntity = new DraftStoreEntity(
            UUID.randomUUID(),
            userId,
            caseId,
            DRAFT_CLAIM_TYPE_ID,
            copyPayload(payload),
            now,
            now,
            now,
            now.plusDays(DRAFT_EXPIRY_DAYS)
        );
        log.info("Creating new draft claim draftId={}", draftStoreEntity.getId());
        return draftStoreRepository.save(draftStoreEntity);
    }

    private Optional<DraftStoreEntity> findDraftClaim(UUID draftId, String userId) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);

        return draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
            draftId,
            userId,
            DRAFT_CLAIM_TYPE_ID,
            OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraftClaim(UUID draftId, String userId) {
        return findDraftClaim(draftId, userId);
    }

    /**
     * Convenience lookup for the current CUI autosave journey.
     * Primary CRUD should use draftId + userId. If duplicate active drafts exist, the latest updated draft wins.
     */
    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getActiveDraftClaimForUser(String userId) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        return draftStoreRepository.findFirstByUserIdAndDraftTypeIdAndExpiresAtAfterOrderByUpdatedAtDesc(
            userId,
            DRAFT_CLAIM_TYPE_ID,
            OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    public DraftStoreEntity updateDraftClaim(UUID draftId,
                                             String userId,
                                             String caseId,
                                             Map<String, Object> payload) {
        return findDraftClaim(draftId, userId)
            .map(existingDraft -> applyDraftClaimUpdate(existingDraft, caseId, payload, OffsetDateTime.now(ZoneOffset.UTC)))
            .orElseThrow(() -> new DraftClaimNotFoundException(draftId));
    }

    public void deleteDraftClaim(UUID draftId, String userId) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        log.info("Deleting draft claim draftId={}", draftId);
        long deleted = draftStoreRepository.deleteByIdAndUserIdAndDraftTypeId(
            draftId,
            userId,
            DRAFT_CLAIM_TYPE_ID
        );
        if (deleted == 0) {
            throw new DraftClaimNotFoundException(draftId);
        }
    }

    private DraftStoreEntity applyDraftClaimUpdate(DraftStoreEntity existingDraft,
                                                   String caseId,
                                                   Map<String, Object> payload,
                                                   OffsetDateTime updatedAt) {
        if (caseId != null) {
            existingDraft.setCaseId(caseId);
        }
        existingDraft.setPayload(copyPayload(payload));
        existingDraft.setUpdatedAt(updatedAt);
        return draftStoreRepository.save(existingDraft);
    }

    private HashMap<String, Object> copyPayload(Map<String, Object> payload) {
        return new HashMap<>(Objects.requireNonNull(payload, "payload must not be null"));
    }
}
