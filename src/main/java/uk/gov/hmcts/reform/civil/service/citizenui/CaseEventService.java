package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;


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

    public String getDefendantResponseSpecEventToken(String authorisation, String userId, String caseId) {
        StartEventResponse eventResponse = startEvent(authorisation, userId, caseId, CaseEvent.DEFENDANT_RESPONSE_SPEC);
        return eventResponse.getToken();
    }
}
