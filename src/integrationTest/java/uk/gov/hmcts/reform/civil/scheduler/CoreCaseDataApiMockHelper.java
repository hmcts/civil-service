package uk.gov.hmcts.reform.civil.scheduler;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ISSUE_DEFAULT_JUDGEMENT_SPEC;

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

    public void mockElasticSearchResult(SearchResult searchResult) {
        when(coreCaseDataApi.searchCases(eq(ACCESS_TOKEN), eq(GENERATED_TOKEN), eq(CASE_TYPE), any(String.class)))
            .thenReturn(searchResult);
    }

    public void mockStartEvent(String caseIdString, StartEventResponse startEventResponse) {
        when(coreCaseDataApi.startEventForCaseWorker(
            ACCESS_TOKEN,
            GENERATED_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            caseIdString,
            ISSUE_DEFAULT_JUDGEMENT_SPEC.name()
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

    public void verifySubmitEvent() {
        verify(coreCaseDataApi).submitEventForCaseWorker(
            eq(ACCESS_TOKEN),
            eq(GENERATED_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            any(String.class),
            eq(true),
            any(CaseDataContent.class)
        );
    }
}
