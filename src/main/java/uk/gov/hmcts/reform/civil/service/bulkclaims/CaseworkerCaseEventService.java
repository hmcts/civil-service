package uk.gov.hmcts.reform.civil.service.bulkclaims;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CaseworkerCaseEventService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    private StartEventResponse startEventCaseworker(String authorisation, String userId, CaseEvent event) {
        return coreCaseDataApi.startForCaseworker(
            authorisation,
            authTokenGenerator.generate(),
            userId,
            JURISDICTION,
            CASE_TYPE,
            event.name()
        );
    }

    public CaseDetails submitEventForNewClaimCaseWorker(CaseworkerEventSubmissionParams params) {
        StartEventResponse eventResponse = startEventCaseworker(params.getAuthorisation(), params.getUserId(), params.getEvent());
        CaseDataContent caseDataContent = caseDataContentFromStartEventResponse(eventResponse, params.getUpdates());
        return coreCaseDataApi.submitForCaseworker(params.getAuthorisation(),
                                                   authTokenGenerator.generate(),
                                                   params.getUserId(),
                                                   JURISDICTION,
                                                   CASE_TYPE,
                                                   true, caseDataContent
        );
    }
}
