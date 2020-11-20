package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.search.Query;
import uk.gov.hmcts.reform.unspec.service.data.UserAuthContent;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.unspec.CaseDefinitionConstants.JURISDICTION;

@Service
@RequiredArgsConstructor
public class CoreCaseDataService {

    private final IdamClient idamClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUpdateUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDetailsConverter caseDetailsConverter;

    public void triggerEvent(Long caseId, CaseEvent eventName) {
        triggerEvent(caseId, eventName, Map.of());
    }

    public void triggerEvent(Long caseId, CaseEvent eventName, Map<String, Object> contentModified) {
        StartEventResponse startEventResponse = startUpdate(caseId.toString(), eventName);
        submitUpdate(caseId.toString(), caseDataContentFromStartEventResponse(startEventResponse, contentModified));
    }

    public StartEventResponse startUpdate(String caseId, CaseEvent eventName) {
        UserAuthContent systemUpdateUser = getSystemUpdateUser();

        return coreCaseDataApi.startEventForCaseWorker(
            systemUpdateUser.getUserToken(),
            authTokenGenerator.generate(),
            systemUpdateUser.getUserId(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            eventName.name()
        );
    }

    public CaseData submitUpdate(String caseId, CaseDataContent caseDataContent) {
        UserAuthContent systemUpdateUser = getSystemUpdateUser();

        CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(
            systemUpdateUser.getUserToken(),
            authTokenGenerator.generate(),
            systemUpdateUser.getUserId(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            true,
            caseDataContent
        );
        return caseDetailsConverter.toCaseData(caseDetails);
    }

    public SearchResult searchCases(Query query) {
        String userToken = idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), CASE_TYPE, query.toString());
    }

    public CaseDetails getCase(Long caseId) {
        String userToken = idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        return coreCaseDataApi.getCase(userToken, authTokenGenerator.generate(), caseId.toString());
    }

    private UserAuthContent getSystemUpdateUser() {
        String userToken = idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = idamClient.getUserInfo(userToken).getUid();
        return UserAuthContent.builder().userToken(userToken).userId(userId).build();
    }

    private CaseDataContent caseDataContentFromStartEventResponse(
        StartEventResponse startEventResponse, Map<String, Object> contentModified) {
        var payload = new HashMap<>(startEventResponse.getCaseDetails().getData());
        payload.putAll(contentModified);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(payload)
            .build();
    }
}
