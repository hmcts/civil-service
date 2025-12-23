package uk.gov.hmcts.reform.civil.ga.service.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class GaCaseEventService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final GaCoreCaseDataService gaCoreCaseDataService;

    public StartEventResponse startEvent(String authorisation, String userId, String caseId, CaseEvent event) {
        return coreCaseDataApi.startEventForCitizen(
            authorisation,
            authTokenGenerator.generate(),
            userId,
            JURISDICTION,
            GENERALAPPLICATION_CASE_TYPE,
            caseId,
            event.name()
        );
    }

    public CaseDetails submitEvent(EventSubmissionParams params) {
        StartEventResponse eventResponse = startEvent(
                params.getAuthorisation(),
                params.getUserId(),
                params.getCaseId(),
                params.getEvent()
        );
        CaseDataContent caseDataContent = gaCoreCaseDataService.caseDataContentFromStartEventResponse(eventResponse, params.getUpdates());
        return coreCaseDataApi.submitEventForCitizen(
                params.getAuthorisation(),
                authTokenGenerator.generate(),
                params.getUserId(),
                JURISDICTION,
                GENERALAPPLICATION_CASE_TYPE,
                params.getCaseId(),
                true,
                caseDataContent
        );
    }
}
