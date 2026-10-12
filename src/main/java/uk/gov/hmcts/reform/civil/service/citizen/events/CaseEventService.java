package uk.gov.hmcts.reform.civil.service.citizen.events;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class CaseEventService {

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
        Optional<UUID> draftId = parseDbDraftId(params);
        if (draftId.isEmpty()) {
            return createNewClaim(params);
        }

        Optional<DraftStoreEntity> draft = draftStoreService.getDraft(
            draftId.get(),
            params.getUserId(),
            DraftType.DRAFT_CLAIM
        );
        if (draft.isPresent() && draft.get().getCaseId() != null) {
            log.info(
                "Returning existing caseId={} for submissionId={}",
                draft.get().getCaseId(),
                params.getSubmissionId()
            );
            return coreCaseDataService.getCase(Long.valueOf(draft.get().getCaseId()), params.getAuthorisation());
        }

        boolean claimed = draftStoreService.claimSubmission(
            draftId.get(),
            params.getUserId(),
            params.getSubmissionId(),
            DraftType.DRAFT_CLAIM
        );
        if (!claimed) {
            return resolveExistingOrConflict(draftId.get(), params);
        }

        CaseDetails created = createNewClaim(params);
        draftStoreService.linkCaseId(
            draftId.get(),
            params.getUserId(),
            created.getId().toString(),
            DraftType.DRAFT_CLAIM
        );
        return created;
    }

    private CaseDetails resolveExistingOrConflict(UUID draftId, EventSubmissionParams params) {
        DraftStoreEntity draft = draftStoreService
            .getDraft(draftId, params.getUserId(), DraftType.DRAFT_CLAIM)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft not found"));

        if (draft.getCaseId() != null) {
            log.info(
                "Returning existing caseId={} after lost claim race for submissionId={}",
                draft.getCaseId(),
                params.getSubmissionId()
            );
            return coreCaseDataService.getCase(Long.valueOf(draft.getCaseId()), params.getAuthorisation());
        }

        log.warn(
            "Submission already claimed for draftId={} submissionId={} existingSubmissionId={}",
            draftId,
            params.getSubmissionId(),
            draft.getSubmissionId()
        );
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Claim submission already in progress for draft " + draftId
        );
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
        if (!params.hasSubmissionId()) {
            return Optional.empty();
        }
        String[] parts = params.getSubmissionId().trim().split(":", 3);
        if (parts.length < 3 || !parts[0].equals(params.getUserId())) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(parts[1]));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
