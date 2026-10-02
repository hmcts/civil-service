package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimNotFoundException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class DraftClaimService {

    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_CLAIM;

    private final DraftStoreService draftStoreService;

    public DraftClaimService(DraftStoreService draftStoreService) {
        this.draftStoreService = draftStoreService;
    }

    public DraftStoreEntity createDraftClaim(String userId, Map<String, Object> payload) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");

        Optional<DraftStoreEntity> existingBlankDraft = draftStoreService.getBlankDraft(userId, DRAFT_TYPE);
        if (existingBlankDraft.isPresent()) {
            DraftStoreEntity draft = existingBlankDraft.get();
            if (isActive(draft, OffsetDateTime.now(ZoneOffset.UTC))) {
                throw new DraftClaimAlreadyExistsException();
            }
            draftStoreService.deleteDraftAndFlush(draft);
        }

        try {
            return draftStoreService.createDraft(userId, null, payload, DRAFT_TYPE);
        } catch (DataIntegrityViolationException ex) {
            // A concurrent request created the blank draft first
            throw new DraftClaimAlreadyExistsException(ex);
        }
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraftClaim(UUID draftId, String userId) {
        return draftStoreService.getDraft(draftId, userId, DRAFT_TYPE);
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getActiveDraftClaimForUser(String userId) {
        return draftStoreService.getActiveBlankDraft(userId, DRAFT_TYPE);
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraftClaimForCase(String userId, String caseId) {
        String normalisedCaseId = normaliseCaseId(caseId);
        if (normalisedCaseId == null) {
            return Optional.empty();
        }
        return draftStoreService.getActiveDraftForCase(userId, normalisedCaseId, DRAFT_TYPE);
    }

    public DraftStoreEntity updateDraftClaim(UUID draftId,
                                             String userId,
                                             String caseId,
                                             Map<String, Object> payload) {
        return draftStoreService.updateDraft(draftId, userId, normaliseCaseId(caseId), payload, DRAFT_TYPE)
            .orElseThrow(() -> new DraftClaimNotFoundException(draftId));
    }

    public void deleteDraftClaim(UUID draftId, String userId) {
        if (!draftStoreService.deleteDraft(draftId, userId, DRAFT_TYPE)) {
            throw new DraftClaimNotFoundException(draftId);
        }
    }

    private static String normaliseCaseId(String caseId) {
        return caseId == null || caseId.isBlank() ? null : caseId;
    }

    private static boolean isActive(DraftStoreEntity draft, OffsetDateTime now) {
        return draft.getExpiresAt().isAfter(now);
    }
}
