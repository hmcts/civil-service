package uk.gov.hmcts.reform.civil.service.citizen.events;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class CaseEventService {

    private static final int CLAIM_CREATED_SKEW_MINUTES = 2;

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DraftStoreService draftStoreService;
    private final CoreCaseDataService coreCaseDataService;
    @Value("${case-flags.logging.enabled:false}")
    private boolean caseFlagsLoggingEnabled;

    private StartEventResponse startEvent(String authorisation, String userId, String caseId, CaseEvent event) {
        return coreCaseDataApi.startEventForCitizen(
            authorisation,
            authTokenGenerator.generate(),
            userId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            event.name()
        );
    }

    private StartEventResponse startEvent(String authorisation, String userId, CaseEvent event) {
        return coreCaseDataApi.startForCitizen(
            authorisation,
            authTokenGenerator.generate(),
            userId,
            JURISDICTION,
            CASE_TYPE,
            event.name()
        );
    }

    public CaseDetails submitEventForClaim(EventSubmissionParams params, boolean includeEventSummary) {
        StartEventResponse eventResponse = startEvent(
            params.getAuthorisation(),
            params.getUserId(),
            params.getCaseId(),
            params.getEvent()
        );
        if (caseFlagsLoggingEnabled) {
            CaseData caseData = caseDetailsConverter.toCaseData(eventResponse.getCaseDetails().getData());
            Flags respondentFlags = caseData.getRespondent1().getFlags();
            log.info("caseid: {}, respondent flags before civil commons call: {}", params.getCaseId(), respondentFlags);
        }
        CaseDataContent caseDataContent = caseDataContentFromStartEventResponse(eventResponse, params.getUpdates());
        if (caseFlagsLoggingEnabled) {
            Map<String, Object> payload = new HashMap<>((Map<String, Object>) caseDataContent.getData());
            Map<String, Object> respondent1 = new HashMap<>((Map<String, Object>) payload.get("respondent1"));
            log.info(
                "caseid: {}, respondent flags after civil commons call: {}",
                params.getCaseId(),
                respondent1.get("flags").toString()
            );
        }
        if (includeEventSummary) {
            CaseData caseData = caseDetailsConverter.toCaseData(params.getUpdates());
            Party respondent1 = caseData.getRespondent1();
            if (respondent1 != null) {
                caseDataContent.getEvent().setSummary("Linked Defendant Email: " + respondent1.getPartyEmail());
                caseDataContent.getEvent().setDescription("Defendant has been Linked");
            }
        }
        return coreCaseDataApi.submitEventForCitizen(
            params.getAuthorisation(),
            authTokenGenerator.generate(),
            params.getUserId(),
            JURISDICTION,
            CASE_TYPE,
            params.getCaseId(),
            true,
            caseDataContent
        );
    }

    public CaseDetails submitEventForNewClaim(EventSubmissionParams params) {
        if (!params.hasSubmissionId()) {
            return createNewClaim(params);
        }

        Optional<DraftStoreEntity> bySubmission = draftStoreService.getDraftBySubmissionId(
            params.getSubmissionId(),
            params.getUserId(),
            DraftType.DRAFT_CLAIM
        );
        if (bySubmission.isPresent() && bySubmission.get().getCaseId() != null) {
            return existingCase(bySubmission.get(), params);
        }

        Optional<UUID> dbDraftId = parseDbDraftId(params);
        if (dbDraftId.isPresent()) {
            return submitClaimedDraft(dbDraftId.get(), params);
        }
        return submitLegacyDraft(bySubmission.orElse(null), params);
    }

    private CaseDetails submitClaimedDraft(UUID draftId, EventSubmissionParams params) {
        Optional<DraftStoreEntity> draft = draftStoreService.getDraft(
            draftId,
            params.getUserId(),
            DraftType.DRAFT_CLAIM
        );
        if (draft.isPresent() && draft.get().getCaseId() != null) {
            return existingCase(draft.get(), params);
        }

        boolean claimed = draftStoreService.claimSubmission(
            draftId,
            params.getUserId(),
            params.getSubmissionId(),
            DraftType.DRAFT_CLAIM
        );
        if (!claimed) {
            return resolveExistingOrConflict(draftId, params);
        }

        return createAndLink(draftId, params);
    }

    private CaseDetails submitLegacyDraft(DraftStoreEntity existingClaim, EventSubmissionParams params) {
        if (existingClaim != null) {
            return resolveExistingOrConflict(existingClaim.getId(), params);
        }

        return draftStoreService.claimLegacySubmission(
                params.getUserId(),
                params.getSubmissionId(),
                DraftType.DRAFT_CLAIM
            )
            .map(claimed -> createAndLink(claimed.getId(), params))
            .orElseGet(() -> resolveExistingOrConflictBySubmissionId(params));
    }

    private CaseDetails createAndLink(UUID draftId, EventSubmissionParams params) {
        CaseDetails created;
        try {
            created = createNewClaim(params);
        } catch (RuntimeException ex) {
            if (isDefinitiveCcdClientFailure(ex)) {
                log.warn(
                    "CCD create failed with client error for draftId={} submissionId={}; releasing submission claim",
                    draftId,
                    params.getSubmissionId()
                );
                draftStoreService.releaseSubmission(draftId, params.getUserId(), DraftType.DRAFT_CLAIM);
            }
            throw ex;
        }
        draftStoreService.linkCaseId(
            draftId,
            params.getUserId(),
            created.getId().toString(),
            DraftType.DRAFT_CLAIM
        );
        return created;
    }

    private static boolean isDefinitiveCcdClientFailure(Throwable ex) {
        if (ex instanceof FeignException feignException) {
            int status = feignException.status();
            return status >= 400 && status < 500;
        }
        return false;
    }

    private CaseDetails existingCase(DraftStoreEntity draft, EventSubmissionParams params) {
        log.info(
            "Returning existing caseId={} for submissionId={}",
            draft.getCaseId(),
            params.getSubmissionId()
        );
        return coreCaseDataService.getCase(Long.valueOf(draft.getCaseId()), params.getAuthorisation());
    }

    private CaseDetails resolveExistingOrConflict(UUID draftId, EventSubmissionParams params) {
        DraftStoreEntity draft = draftStoreService
            .getDraft(draftId, params.getUserId(), DraftType.DRAFT_CLAIM)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft not found"));

        if (draft.getCaseId() != null) {
            return existingCase(draft, params);
        }

        return reconcileOrConflict(draft, params);
    }

    private CaseDetails resolveExistingOrConflictBySubmissionId(EventSubmissionParams params) {
        DraftStoreEntity draft = draftStoreService
            .getDraftBySubmissionId(params.getSubmissionId(), params.getUserId(), DraftType.DRAFT_CLAIM)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft not found"));

        if (draft.getCaseId() != null) {
            return existingCase(draft, params);
        }

        return reconcileOrConflict(draft, params);
    }

    private CaseDetails reconcileOrConflict(DraftStoreEntity draft, EventSubmissionParams params) {
        if (params.getSubmissionId().equals(draft.getSubmissionId())) {
            Optional<CaseDetails> recovered = recoverUnlinkedCcdCase(draft, params);
            if (recovered.isPresent()) {
                return recovered.get();
            }
        }

        log.warn(
            "Submission already claimed for draftId={} submissionId={} existingSubmissionId={}",
            draft.getId(),
            params.getSubmissionId(),
            draft.getSubmissionId()
        );
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Claim submission already in progress for draft " + draft.getId()
        );
    }

    private Optional<CaseDetails> recoverUnlinkedCcdCase(DraftStoreEntity draft, EventSubmissionParams params) {
        Set<String> linkedCaseIds = draftStoreService.getLinkedCaseIds(
            params.getUserId(),
            DraftType.DRAFT_CLAIM
        );
        SearchResult searchResult = coreCaseDataService.getCCDClaimsForLipClaimant(params.getAuthorisation(), 0);
        if (searchResult == null || searchResult.getCases() == null) {
            return Optional.empty();
        }

        OffsetDateTime claimedAt = draft.getUpdatedAt() != null
            ? draft.getUpdatedAt()
            : draft.getCreatedAt();
        List<CaseDetails> candidates = searchResult.getCases().stream()
            .filter(caseDetails -> caseDetails.getId() != null)
            .filter(caseDetails -> !linkedCaseIds.contains(caseDetails.getId().toString()))
            .filter(caseDetails -> wasCreatedAtOrAfterClaim(caseDetails, claimedAt))
            .toList();

        if (candidates.size() != 1) {
            log.info(
                "Could not uniquely recover CCD case for draftId={} submissionId={} candidates={}",
                draft.getId(),
                params.getSubmissionId(),
                candidates.size()
            );
            return Optional.empty();
        }

        CaseDetails recovered = candidates.getFirst();
        String caseId = recovered.getId().toString();
        log.info(
            "Recovered CCD caseId={} for draftId={} submissionId={} after lost create response",
            caseId,
            draft.getId(),
            params.getSubmissionId()
        );
        draftStoreService.linkCaseId(draft.getId(), params.getUserId(), caseId, DraftType.DRAFT_CLAIM);
        return Optional.of(coreCaseDataService.getCase(recovered.getId(), params.getAuthorisation()));
    }

    private static boolean wasCreatedAtOrAfterClaim(CaseDetails caseDetails, OffsetDateTime claimedAt) {
        LocalDateTime createdDate = caseDetails.getCreatedDate();
        if (createdDate == null || claimedAt == null) {
            return false;
        }
        OffsetDateTime createdAt = createdDate.atZone(ZoneOffset.UTC).toOffsetDateTime();
        return !createdAt.isBefore(claimedAt.minusMinutes(CLAIM_CREATED_SKEW_MINUTES));
    }

    private CaseDetails createNewClaim(EventSubmissionParams params) {
        StartEventResponse eventResponse = startEvent(params.getAuthorisation(), params.getUserId(), params.getEvent());
        CaseDataContent caseDataContent = caseDataContentFromStartEventResponse(eventResponse, params.getUpdates());
        return coreCaseDataApi.submitForCitizen(params.getAuthorisation(),
                                                authTokenGenerator.generate(),
                                                params.getUserId(),
                                                JURISDICTION,
                                                CASE_TYPE,
                                                true, caseDataContent
        );
    }

    public CaseDetails submitEvent(EventSubmissionParams params) {
        if (params.isDraftClaim()) {
            return submitEventForNewClaim(params);
        }
        return submitEventForClaim(params, false);
    }

    private Optional<UUID> parseDbDraftId(EventSubmissionParams params) {
        String[] parts = params.getSubmissionId().trim().split(":", 3);
        if (parts.length < 3 || !parts[0].equals(params.getUserId())) {
            return Optional.empty();
        }
        if (parts[1].equals(params.getUserId())) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(parts[1]));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
