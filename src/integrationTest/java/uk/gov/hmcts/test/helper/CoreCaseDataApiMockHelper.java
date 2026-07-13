package uk.gov.hmcts.test.helper;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;

public class CoreCaseDataApiMockHelper {

    private static final String ACCESS_TOKEN = "mockedAccessToken";
    private static final String GENERATED_TOKEN = "generatedToken";
    private static final String USER_ID = UUID.randomUUID().toString();

    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamClient idamClient;
    private final AuthTokenGenerator authTokenGenerator;

    public CoreCaseDataApiMockHelper(CoreCaseDataApi coreCaseDataApi,
                                     IdamClient idamClient,
                                     AuthTokenGenerator authTokenGenerator) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.idamClient = idamClient;
        this.authTokenGenerator = authTokenGenerator;
    }

    public void setupIdamClient() {
        when(idamClient.getAccessToken(any(), any())).thenReturn(ACCESS_TOKEN);
        when(idamClient.getUserInfo(ACCESS_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
        when(authTokenGenerator.generate()).thenReturn(GENERATED_TOKEN);
    }

    public void resetMocks() {
        reset(coreCaseDataApi, idamClient, authTokenGenerator);
    }

    public void mockElasticSearchResult(SearchResult searchResult) {
        when(coreCaseDataApi.searchCases(eq(ACCESS_TOKEN), eq(GENERATED_TOKEN), eq(CASE_TYPE), any(String.class)))
            .thenReturn(searchResult);
    }

    public void mockElasticSearchResultPaginated(SearchResult searchResult, SearchResult... nextSearchResults) {
        when(coreCaseDataApi.searchCases(eq(ACCESS_TOKEN), eq(GENERATED_TOKEN), eq(CASE_TYPE), any()))
            .thenReturn(searchResult, nextSearchResults);
    }

    public void mockGetCase(String caseIdString, CaseDetails caseDetails) {
        when(coreCaseDataApi.getCase(eq(ACCESS_TOKEN), eq(GENERATED_TOKEN), eq(caseIdString))).thenReturn(caseDetails);
    }

    public void mockStartEvent(String caseIdString, StartEventResponse startEventResponse, String eventId) {
        when(coreCaseDataApi.startEventForCaseWorker(
            eq(ACCESS_TOKEN),
            eq(GENERATED_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(caseIdString),
            eq(eventId)
        )).thenReturn(startEventResponse);
    }

    public void mockStartEventAnyCase(StartEventResponse startEventResponse, String eventId) {
        when(coreCaseDataApi.startEventForCaseWorker(
            eq(ACCESS_TOKEN),
            eq(GENERATED_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            any(String.class),
            eq(eventId)
        )).thenReturn(startEventResponse);
    }

    public void mockSubmitEvent(String caseIdString, CaseDetails caseDetails) {
        when(coreCaseDataApi.submitEventForCaseWorker(
            eq(ACCESS_TOKEN),
            eq(GENERATED_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(caseIdString),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(caseDetails);
    }

    public void mockSubmitEventAnyCase(CaseDetails caseDetails) {
        when(coreCaseDataApi.submitEventForCaseWorker(
            eq(ACCESS_TOKEN),
            eq(GENERATED_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            any(String.class),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(caseDetails);
    }

    public void verifySubmitEvent(int expectedCount) {
        verify(coreCaseDataApi, org.mockito.Mockito.times(expectedCount)).submitEventForCaseWorker(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            anyBoolean(),
            any()
        );
    }
}
