package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftGeneralApplicationAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftGeneralApplicationNotFoundException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class DraftGeneralApplicationService {

    private static final DraftType APPLICATION = DraftType.DRAFT_GENERAL_APPLICATION;
    private static final DraftType RESPONSE = DraftType.DRAFT_GENERAL_APPLICATION_RESPONSE;

    private final DraftStoreService draftStoreService;

    public DraftGeneralApplicationService(DraftStoreService draftStoreService) {
        this.draftStoreService = draftStoreService;
    }

    public DraftStoreEntity create(String userId, String caseId, String applicationId, Map<String, Object> payload) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        String normalisedCaseId = requireCaseId(caseId);
        String normalisedApplicationId = StringUtils.trimToNull(applicationId);
        DraftType draftType = draftTypeFor(normalisedApplicationId);
        replaceExpiredDraft(userId, normalisedCaseId, normalisedApplicationId, draftType);

        try {
            return draftStoreService.createDraft(
                userId,
                normalisedCaseId,
                normalisedApplicationId,
                payload,
                draftType
            );
        } catch (DataIntegrityViolationException ex) {
            throw new DraftGeneralApplicationAlreadyExistsException(reference(normalisedCaseId, normalisedApplicationId), ex);
        }
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> get(UUID draftId, String userId) {
        Optional<DraftStoreEntity> application = draftStoreService.getDraft(draftId, userId, APPLICATION);
        if (application.isPresent()) {
            return application;
        }
        return draftStoreService.getDraft(draftId, userId, RESPONSE);
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getApplicationForCase(String userId, String caseId) {
        String normalisedCaseId = StringUtils.trimToNull(caseId);
        if (normalisedCaseId == null) {
            return Optional.empty();
        }
        return draftStoreService.getActiveDraftForCase(userId, normalisedCaseId, APPLICATION);
    }

    @Transactional(readOnly = true)
    public List<DraftStoreEntity> getResponsesForCase(String userId, String caseId) {
        String normalisedCaseId = StringUtils.trimToNull(caseId);
        if (normalisedCaseId == null) {
            return List.of();
        }
        return draftStoreService.listActiveDraftsForCase(userId, normalisedCaseId, RESPONSE);
    }

    @Transactional(readOnly = true)
    public Optional<DraftStoreEntity> getResponseForApplication(String userId, String applicationId) {
        String normalisedApplicationId = StringUtils.trimToNull(applicationId);
        if (normalisedApplicationId == null) {
            return Optional.empty();
        }
        return draftStoreService.getActiveDraftForApplication(userId, normalisedApplicationId, RESPONSE);
    }

    public DraftStoreEntity update(UUID draftId, String userId, String caseId, Map<String, Object> payload) {
        DraftStoreEntity existing = get(draftId, userId)
            .orElseThrow(() -> new DraftGeneralApplicationNotFoundException(draftId));
        return draftStoreService.updateDraft(
            draftId,
            userId,
            requireCaseId(caseId),
            payload,
            existing.getDraftType()
        ).orElseThrow(() -> new DraftGeneralApplicationNotFoundException(draftId));
    }

    public void delete(UUID draftId, String userId) {
        if (draftStoreService.deleteDraft(draftId, userId, APPLICATION)) {
            return;
        }
        if (!draftStoreService.deleteDraft(draftId, userId, RESPONSE)) {
            throw new DraftGeneralApplicationNotFoundException(draftId);
        }
    }

    public void deleteApplicationForCase(String userId, String caseId) {
        getApplicationForCase(userId, caseId)
            .ifPresent(draft -> draftStoreService.deleteDraft(draft.getId(), userId, APPLICATION));
    }

    public void deleteResponseForApplication(String userId, String applicationId) {
        getResponseForApplication(userId, applicationId)
            .ifPresent(draft -> draftStoreService.deleteDraft(draft.getId(), userId, RESPONSE));
    }

    public void deleteApplicationDraftsForUser(String userId) {
        List<DraftStoreEntity> drafts = draftStoreService.listActiveDraftsForUser(userId, APPLICATION);
        for (DraftStoreEntity draft : drafts) {
            draftStoreService.deleteDraft(draft.getId(), userId, APPLICATION);
        }
    }

    private void replaceExpiredDraft(String userId, String caseId, String applicationId, DraftType draftType) {
        Optional<DraftStoreEntity> existing = draftType == APPLICATION
            ? draftStoreService.getDraftForCase(userId, caseId, draftType)
            : draftStoreService.getDraftForApplication(userId, applicationId, draftType);
        if (existing.isEmpty()) {
            return;
        }
        DraftStoreEntity draft = existing.get();
        if (draft.getExpiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new DraftGeneralApplicationAlreadyExistsException(reference(caseId, applicationId));
        }
        draftStoreService.deleteDraft(draft.getId(), userId, draftType);
    }

    private static DraftType draftTypeFor(String applicationId) {
        return applicationId == null ? APPLICATION : RESPONSE;
    }

    private static String requireCaseId(String caseId) {
        String normalised = StringUtils.trimToNull(caseId);
        if (normalised == null) {
            throw new IllegalArgumentException("caseId must not be blank");
        }
        return normalised;
    }

    private static String reference(String caseId, String applicationId) {
        return applicationId == null ? "caseId " + caseId : "applicationId " + applicationId;
    }
}
