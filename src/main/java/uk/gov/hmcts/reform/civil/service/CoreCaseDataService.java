package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@Service
@RequiredArgsConstructor
public class CoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUpdateUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDetailsConverter caseDetailsConverter;
    private final UserService userService;

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

    public SearchResult searchCases(Query query, String authorization) {
        return coreCaseDataApi.searchCases(authorization, authTokenGenerator.generate(), CASE_TYPE, query.toString());
    }

    public SearchResult searchCases(Query query) {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), CASE_TYPE, query.toString());
    }

    public CaseDetails getCase(Long caseId) {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        return coreCaseDataApi.getCase(userToken, authTokenGenerator.generate(), caseId.toString());
    }

    public CaseDetails getCase(Long caseId, String authorisation) {
        return coreCaseDataApi.getCase(authorisation, authTokenGenerator.generate(), caseId.toString());
    }

    private UserAuthContent getSystemUpdateUser() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = userService.getUserInfo(userToken).getUid();
        return UserAuthContent.builder().userToken(userToken).userId(userId).build();
    }
}
