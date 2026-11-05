package uk.gov.hmcts.reform.dashboard.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseNotFoundException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DraftDefendantResponseService {

    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_DEFENDANT_RESPONSE;

    private final DraftStoreService draftStoreService;

    public DraftDefendantResponseService(DraftStoreService draftStoreService) {
        this.draftStoreService = draftStoreService;
    }

    public DraftStoreEntity createDraftResponse(String userId, String caseId, Map<String, Object> payload) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        String normalisedCaseId = normaliseCaseId(caseId);
        if (normalisedCaseId == null) {
            throw new IllegalArgumentException("caseId must not be blank");
        }
        try {
            return draftStoreService.createDraft(userId, normalisedCaseId, payload, DRAFT_TYPE);
        } catch (DataIntegrityViolationException ex) {
            throw new DraftDefendantResponseAlreadyExistsException(ex);
        }
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraftResponse(UUID draftId, String userId) {
        return draftStoreService.getDraft(draftId, userId, DRAFT_TYPE);
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getDraftResponseForCase(String userId, String caseId) {
        String normalisedCaseId = normaliseCaseId(caseId);
        if (normalisedCaseId == null) {
            return Optional.empty();
        }
        return draftStoreService.getActiveDraftForCase(userId, normalisedCaseId, DRAFT_TYPE);
    }

    public DraftStoreEntity updateResponseDraft(UUID draftId,
                                             String userId,
                                             String caseId,
                                             Map<String, Object> payload) {
        return draftStoreService.updateDraft(draftId, userId, normaliseCaseId(caseId), payload, DRAFT_TYPE)
            .orElseThrow(() -> new DraftDefendantResponseNotFoundException(draftId));
    }

    public void deleteDraftResponse(UUID draftId, String userId) {
        if (!draftStoreService.deleteDraft(draftId, userId, DRAFT_TYPE)) {
            throw new DraftDefendantResponseNotFoundException(draftId);
        }
    }

    private static String normaliseCaseId(String caseId) {
        return StringUtils.trimToNull(caseId);
    }
}
