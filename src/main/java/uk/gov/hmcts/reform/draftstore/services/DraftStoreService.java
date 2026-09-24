package uk.gov.hmcts.reform.draftstore.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    static final String TTL_DAYS_FIELD = "draftClaimCacheTtlDays";
    static final long DEFAULT_TTL_DAYS = 30L;

    private final DraftStoreRepository draftStoreRepository;
    private final DraftStoreTransactionService draftStoreTransactionService;

    public DraftStoreService(DraftStoreRepository draftStoreRepository,
                             DraftStoreTransactionService draftStoreTransactionService) {
        this.draftStoreRepository = draftStoreRepository;
        this.draftStoreTransactionService = draftStoreTransactionService;
    }

    public DraftStoreEntity createDraft(String userId,
                                        String caseId,
                                        Map<String, Object> payload,
                                        DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        Map<String, Object> payloadCopy = copyPayload(payload);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        DraftStoreEntity draft = new DraftStoreEntity(
            UUID.randomUUID(),
            userId,
            caseId,
            draftType,
            payloadCopy,
            now,
            now,
            now.plusDays(resolveTtlDays(payloadCopy))
        );
        try {
            return draftStoreTransactionService.saveInNewTransaction(draft);
        } catch (DataIntegrityViolationException ex) {
            return getActiveDraftsForUser(userId, draftType).stream()
                .findFirst()
                .orElseThrow(() -> ex);
        }
    }

    @Transactional(readOnly = true)
    public List<DraftStoreEntity> getDraftsForUser(String userId, DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByUserIdAndDraftType(userId, draftType);
    }

    @Transactional(readOnly = true)
    public List<DraftStoreEntity> getActiveDraftsForUser(String userId, DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByUserIdAndDraftTypeAndExpiresAtAfter(
            userId,
            draftType,
            OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraft(UUID draftId, String userId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
            draftId,
            userId,
            draftType,
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
        return draftStoreTransactionService.deleteByIdInNewTransaction(
            draftId,
            userId,
            draftType
        ) > 0;
    }

    public void deleteDraftAndFlush(DraftStoreEntity draft) {
        draftStoreTransactionService.deleteInNewTransaction(draft);
    }

    private DraftStoreEntity applyDraftUpdate(DraftStoreEntity existingDraft,
                                              String caseId,
                                              Map<String, Object> payload) {
        if (caseId != null) {
            existingDraft.setCaseId(caseId);
        }
        existingDraft.setPayload(copyPayload(payload));
        existingDraft.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return draftStoreTransactionService.saveInNewTransaction(existingDraft);
    }

    private long resolveTtlDays(Map<String, Object> payload) {
        Object value = payload.get(TTL_DAYS_FIELD);
        Long ttlDays = null;
        if (value instanceof Number number) {
            ttlDays = number.longValue();
        } else if (value instanceof String text) {
            try {
                ttlDays = Long.parseLong(text.trim());
            } catch (NumberFormatException ex) {
                ttlDays = null;
            }
        }
        if (ttlDays == null || ttlDays <= 0) {
            log.warn("Missing or invalid {} in draft payload (value={}), defaulting to {} days",
                     TTL_DAYS_FIELD, value, DEFAULT_TTL_DAYS);
            return DEFAULT_TTL_DAYS;
        }
        return ttlDays;
    }

    private Map<String, Object> copyPayload(Map<String, Object> payload) {
        return new HashMap<>(Objects.requireNonNull(payload, "payload must not be null"));
    }
}
