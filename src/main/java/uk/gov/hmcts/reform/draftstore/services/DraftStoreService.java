package uk.gov.hmcts.reform.draftstore.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class DraftStoreService {

    private static final String USER_ID_NOT_NULL = "userId must not be null";
    private static final String DRAFT_TYPE_NOT_NULL = "draftType must not be null";

    private final DraftStoreRepository draftStoreRepository;

    public DraftStoreService(DraftStoreRepository draftStoreRepository) {
        this.draftStoreRepository = draftStoreRepository;
    }

    public DraftStoreEntity createDraft(String userId,
                                        String caseId,
                                        Map<String, Object> payload,
                                        DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        DraftStoreEntity draft = new DraftStoreEntity(
            UUID.randomUUID(),
            userId,
            caseId,
            draftType.getId(),
            copyPayload(payload),
            now,
            now,
            draftType.calculateExpiry(now)
        );
        log.info("Creating draft type={} draftId={}", draftType, draft.getId());
        return draftStoreRepository.save(draft);
    }

    @Transactional(readOnly = true)
    public List<DraftStoreEntity> getDraftsForUser(String userId, DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByUserIdAndDraftTypeId(userId, draftType.getId());
    }

    @Transactional(readOnly = true)
    public List<DraftStoreEntity> getActiveDraftsForUser(String userId, DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByUserIdAndDraftTypeIdAndExpiresAtAfter(
            userId,
            draftType.getId(),
            OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraft(UUID draftId, String userId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
            draftId,
            userId,
            draftType.getId(),
            OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    public Optional<DraftStoreEntity> updateDraft(UUID draftId,
                                                  String userId,
                                                  String caseId,
                                                  Map<String, Object> payload,
                                                  DraftType draftType) {
        return getDraft(draftId, userId, draftType)
            .map(existingDraft -> applyDraftUpdate(existingDraft, caseId, payload));
    }

    public boolean deleteDraft(UUID draftId, String userId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        log.info("Deleting draft type={} draftId={}", draftType, draftId);
        return draftStoreRepository.deleteByIdAndUserIdAndDraftTypeId(
            draftId,
            userId,
            draftType.getId()
        ) > 0;
    }

    public void deleteDraftAndFlush(DraftStoreEntity draft) {
        Objects.requireNonNull(draft, "draft must not be null");
        log.info("Deleting expired draft typeId={} draftId={}", draft.getDraftTypeId(), draft.getId());
        draftStoreRepository.delete(draft);
        draftStoreRepository.flush();
    }

    private DraftStoreEntity applyDraftUpdate(DraftStoreEntity existingDraft,
                                              String caseId,
                                              Map<String, Object> payload) {
        if (caseId != null) {
            existingDraft.setCaseId(caseId);
        }
        existingDraft.setPayload(copyPayload(payload));
        existingDraft.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return draftStoreRepository.save(existingDraft);
    }

    private Map<String, Object> copyPayload(Map<String, Object> payload) {
        return new HashMap<>(Objects.requireNonNull(payload, "payload must not be null"));
    }
}
