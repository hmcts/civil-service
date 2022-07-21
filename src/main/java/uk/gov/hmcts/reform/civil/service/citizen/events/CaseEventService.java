package uk.gov.hmcts.reform.civil.service.citizen.events;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@Service
@AllArgsConstructor
public class CaseEventService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

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

    public CaseDetails submitEvent(EventSubmissionParams params) {
        StartEventResponse eventResponse = startEvent(
            params.getAuthorisation(),
            params.getUserId(),
            params.getCaseId(),
            params.getEvent()
        );
        CaseDataContent caseDataContent = caseDataContentFromStartEventResponse(eventResponse, params.getUpdates());
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
}
