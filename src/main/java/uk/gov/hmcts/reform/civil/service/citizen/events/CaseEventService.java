package uk.gov.hmcts.reform.civil.service.citizen.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

import java.util.HashMap;
import java.util.Map;

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

    public CaseDetails submitEventForClaim(EventSubmissionParams params) {
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
            var payload = new HashMap((Map) caseDataContent.getData());
            var respondent1 = new HashMap((Map) payload.get("respondent1"));
            log.info(
                "caseid: {}, respondent flags after civil commons call: {}",
                params.getCaseId(),
                respondent1.get("flags").toString()
            );
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
        return submitEventForClaim(params);
    }
}
