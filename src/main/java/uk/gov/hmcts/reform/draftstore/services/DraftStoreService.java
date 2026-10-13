package uk.gov.hmcts.reform.draftstore.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    private static final String USER_ID_NOT_NULL = "userId must not be null";
    private static final String DRAFT_TYPE_NOT_NULL = "draftType must not be null";
    private static final ZoneId EXPIRY_ZONE = ZoneId.of("Europe/London");
    static final String TTL_DAYS_FIELD = "draftClaimCacheTtlDays";

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
        validatePayloadRetention(payloadCopy, draftType);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        long retentionDays = draftType.getRetentionDays();

        DraftStoreEntity draft = new DraftStoreEntity(
            UUID.randomUUID(),
            userId,
            caseId,
            draftType,
            payloadCopy,
            now,
            now,
            calculateExpiresAt(now, retentionDays),
            null
        );
        return draftStoreTransactionService.saveInNewTransaction(draft);
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getBlankDraft(String userId, DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByUserIdAndDraftTypeAndCaseIdIsNull(userId, draftType);
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getActiveBlankDraft(String userId, DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByUserIdAndDraftTypeAndCaseIdIsNullAndExpiresAtAfter(
            userId,
            draftType,
            OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getActiveDraftForCase(String userId, String caseId, DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(caseId, "caseId must not be null");
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findByUserIdAndDraftTypeAndCaseIdAndExpiresAtAfter(
            userId,
            draftType,
            caseId,
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

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraftBySubmissionId(String submissionId,
                                                             String userId,
                                                             DraftType draftType) {
        Objects.requireNonNull(submissionId, "submissionId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreRepository.findBySubmissionIdAndUserIdAndDraftTypeAndExpiresAtAfter(
            submissionId,
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
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return getDraft(draftId, userId, draftType)
            .map(existingDraft -> {
                validatePayloadRetention(payload, draftType);
                return applyDraftUpdate(existingDraft, caseId, payload);
            });
    }

    public Optional<DraftStoreEntity> linkCaseId(UUID draftId, String userId, String caseId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(caseId, "caseId must not be null");
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        int linked = draftStoreTransactionService.linkCaseIdInNewTransaction(
            draftId,
            userId,
            draftType,
            caseId,
            OffsetDateTime.now(ZoneOffset.UTC)
        );
        if (linked == 0) {
            return Optional.empty();
        }
        return getDraft(draftId, userId, draftType);
    }

    /**
     * Links the user's blank draft that is mid-submission ({@code submission_id} set,
     * {@code case_id} null) to the CCD case just created for that claimant.
     * Used from CREATE_LIP_CLAIM submitted callback so a lost create response can still
     * be recovered on retry via {@code submission_id}. No-ops when there is no claimed blank draft.
     */
    public Optional<DraftStoreEntity> linkClaimedBlankDraftToCase(String userId,
                                                                  String caseId,
                                                                  DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(caseId, "caseId must not be null");
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);

        Optional<DraftStoreEntity> blank = getBlankDraft(userId, draftType);
        if (blank.isEmpty() || blank.get().getSubmissionId() == null) {
            return Optional.empty();
        }
        DraftStoreEntity draft = blank.get();
        log.info(
            "Linking claimed blank draftId={} submissionId={} to caseId={}",
            draft.getId(),
            draft.getSubmissionId(),
            caseId
        );
        return linkCaseId(draft.getId(), userId, caseId, draftType);
    }

    /**
     * Claims the draft for this submission before CCD create. Returns true only for the
     * caller that successfully set {@code submission_id} while {@code case_id} was still null.
     */
    public boolean claimSubmission(UUID draftId, String userId, String submissionId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(submissionId, "submissionId must not be null");
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreTransactionService.claimSubmissionInNewTransaction(
            draftId,
            userId,
            draftType,
            submissionId,
            OffsetDateTime.now(ZoneOffset.UTC)
        ) == 1;
    }

    public boolean releaseSubmission(UUID draftId, String userId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        return draftStoreTransactionService.releaseSubmissionInNewTransaction(
            draftId,
            userId,
            draftType,
            OffsetDateTime.now(ZoneOffset.UTC)
        ) == 1;
    }

    /**
     * After a definitive CCD create failure, free the submission so retry can proceed.
     * Real drafts clear {@code submission_id}. Empty legacy Redis claim receipts are deleted so
     * they do not block the unique active-draft index on the next attempt.
     */
    public void releaseFailedSubmissionClaim(UUID draftId, String userId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);

        Optional<DraftStoreEntity> draft = getDraft(draftId, userId, draftType);
        if (draft.isEmpty() || draft.get().getCaseId() != null) {
            return;
        }
        if (isLegacyClaimReceipt(draft.get())) {
            log.info("Deleting legacy claim receipt after failed CCD create draftId={}", draftId);
            deleteDraft(draftId, userId, draftType);
            return;
        }
        releaseSubmission(draftId, userId, draftType);
    }

    public Optional<DraftStoreEntity> claimLegacySubmission(String userId,
                                                            String submissionId,
                                                            DraftType draftType) {
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(submissionId, "submissionId must not be null");
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        DraftStoreEntity claimRow = new DraftStoreEntity(
            UUID.randomUUID(),
            userId,
            null,
            draftType,
            new HashMap<>(),
            now,
            now,
            calculateExpiresAt(now, draftType.getRetentionDays()),
            submissionId
        );
        try {
            return Optional.of(draftStoreTransactionService.saveInNewTransaction(claimRow));
        } catch (DataIntegrityViolationException ex) {
            return reuseBlankDraftForLegacySubmission(userId, submissionId, draftType)
                .or(() -> {
                    log.info(
                        "Lost legacy submission claim for userId={} submissionId={}",
                        userId,
                        submissionId
                    );
                    return Optional.empty();
                });
        }
    }

    private Optional<DraftStoreEntity> reuseBlankDraftForLegacySubmission(String userId,
                                                                          String submissionId,
                                                                          DraftType draftType) {
        Optional<DraftStoreEntity> blank = getBlankDraft(userId, draftType);
        if (blank.isEmpty() || blank.get().getCaseId() != null) {
            return Optional.empty();
        }
        UUID draftId = blank.get().getId();
        if (submissionId.equals(blank.get().getSubmissionId())) {
            return getDraft(draftId, userId, draftType);
        }
        if (blank.get().getSubmissionId() != null) {
            return Optional.empty();
        }
        if (!claimSubmission(draftId, userId, submissionId, draftType)) {
            return Optional.empty();
        }
        log.info("Reused blank draft receipt draftId={} for legacy submissionId={}", draftId, submissionId);
        return getDraft(draftId, userId, draftType);
    }

    private static boolean isLegacyClaimReceipt(DraftStoreEntity draft) {
        Map<String, Object> payload = draft.getPayload();
        return payload == null || payload.isEmpty();
    }

    public Optional<DraftStoreEntity> applyPaymentRetention(UUID draftId, String userId, DraftType draftType) {
        Objects.requireNonNull(draftId, "draftId must not be null");
        Objects.requireNonNull(userId, USER_ID_NOT_NULL);
        Objects.requireNonNull(draftType, DRAFT_TYPE_NOT_NULL);

        return getDraft(draftId, userId, draftType).map(draft -> {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            OffsetDateTime paymentExpiresAt = calculateExpiresAt(now, draftType.getPaymentRetentionDays());
            if (!paymentExpiresAt.isBefore(draft.getExpiresAt())) {
                return draft;
            }
            draftStoreTransactionService.updateExpiresAtInNewTransaction(
                draftId,
                userId,
                draftType,
                paymentExpiresAt,
                now
            );
            draft.setExpiresAt(paymentExpiresAt);
            draft.setUpdatedAt(now);
            return draft;
        });
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

    private Map<String, Object> copyPayload(Map<String, Object> payload) {
        return new HashMap<>(Objects.requireNonNull(payload, "payload must not be null"));
    }

    private static void validatePayloadRetention(Map<String, Object> payload, DraftType draftType) {
        Objects.requireNonNull(payload, "payload must not be null");
        if (!payload.containsKey(TTL_DAYS_FIELD)) {
            return;
        }
        Object value = payload.get(TTL_DAYS_FIELD);
        Long ttlDays = parseTtlDays(value);
        long expectedDays = draftType.getRetentionDays();
        if (ttlDays == null || ttlDays != expectedDays) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                TTL_DAYS_FIELD + " must be " + expectedDays + " for draft type " + draftType
            );
        }
    }

    private static Long parseTtlDays(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    public static OffsetDateTime calculateExpiresAt(OffsetDateTime createdAt, long ttlDays) {
        LocalDate expiryDate = createdAt
            .atZoneSameInstant(EXPIRY_ZONE)
            .toLocalDate()
            .plusDays(ttlDays + 1);
        return expiryDate.atStartOfDay(EXPIRY_ZONE).toOffsetDateTime();
    }
}
