package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public DraftClaimCreationResult createDraftClaim(String userId, String caseId, Map<String, Object> payload) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Optional<DraftStoreEntity> existingDraft = draftStoreService.getDraftsForUser(userId, DRAFT_TYPE)
            .stream()
            .findFirst();
        if (existingDraft.isPresent()) {
            DraftStoreEntity draft = existingDraft.get();
            if (draft.getExpiresAt().isAfter(now)) {
                log.info("Returning existing active draft claim draftId={}", draft.getId());
                return DraftClaimCreationResult.existingDraft(draft);
            }
            draftStoreService.deleteDraftAndFlush(draft);
        }

        return DraftClaimCreationResult.newDraft(
            draftStoreService.createDraft(userId, caseId, payload, DRAFT_TYPE)
        );
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraftClaim(UUID draftId, String userId) {
        return draftStoreService.getDraft(draftId, userId, DRAFT_TYPE);
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getActiveDraftClaimForUser(String userId) {
        return draftStoreService.getActiveDraftsForUser(userId, DRAFT_TYPE)
            .stream()
            .findFirst();
    }

    public DraftStoreEntity updateDraftClaim(UUID draftId,
                                             String userId,
                                             String caseId,
                                             Map<String, Object> payload) {
        return draftStoreService.updateDraft(draftId, userId, caseId, payload, DRAFT_TYPE)
            .orElseThrow(() -> new DraftClaimNotFoundException(draftId));
    }

    public void deleteDraftClaim(UUID draftId, String userId) {
        if (!draftStoreService.deleteDraft(draftId, userId, DRAFT_TYPE)) {
            throw new DraftClaimNotFoundException(draftId);
        }
    }
}
