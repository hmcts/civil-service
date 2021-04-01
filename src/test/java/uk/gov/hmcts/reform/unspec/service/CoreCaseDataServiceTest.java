package uk.gov.hmcts.reform.unspec.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.search.Query;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    CoreCaseDataService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class CoreCaseDataServiceTest {

    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final String CASE_TYPE = "UNSPECIFIED_CLAIMS";

    @MockBean
    private SystemUpdateUserConfiguration userConfig;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CoreCaseDataService service;

    @BeforeEach
    void init() {
        clearInvocations(authTokenGenerator);
        clearInvocations(idamClient);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);
    }

    @Nested
    class TriggerEvent {

        private static final String EVENT_ID = "DISMISS_CLAIM";
        private static final String JURISDICTION = "CIVIL";
        private static final String EVENT_TOKEN = "eventToken";
        private static final String CASE_ID = "1";
        private static final String USER_ID = "User1";
        private final CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();
        private final CaseDetails caseDetails = CaseDetailsBuilder.builder()
            .data(caseData)
            .build();

        @BeforeEach
        void setUp() {
            when(idamClient.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());

            when(coreCaseDataApi.startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION,
                                                         CASE_TYPE, CASE_ID, EVENT_ID
            )).thenReturn(buildStartEventResponse());

            when(coreCaseDataApi.submitEventForCaseWorker(
                eq(USER_AUTH_TOKEN),
                eq(SERVICE_AUTH_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION),
                eq(CASE_TYPE),
                eq(CASE_ID),
                anyBoolean(),
                any(CaseDataContent.class)
                 )
            ).thenReturn(caseDetails);
        }

        @Test
        void shouldStartAndSubmitEvent_WhenCalled() {
            service.triggerEvent(Long.valueOf(CASE_ID), CaseEvent.valueOf(EVENT_ID));

            verify(coreCaseDataApi).startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID,
                                                            JURISDICTION, CASE_TYPE, CASE_ID, EVENT_ID
            );
            verify(coreCaseDataApi).submitEventForCaseWorker(
                eq(USER_AUTH_TOKEN),
                eq(SERVICE_AUTH_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION),
                eq(CASE_TYPE),
                eq(CASE_ID),
                anyBoolean(),
                any(CaseDataContent.class)
            );
        }

        private StartEventResponse buildStartEventResponse() {
            return StartEventResponse.builder()
                .eventId(EVENT_ID)
                .token(EVENT_TOKEN)
                .caseDetails(caseDetails)
                .build();
        }
    }

    @Nested
    class SearchCases {

        @Test
        void shouldReturnCases_WhenSearchingCasesAsSystemUpdateUser() {
            Query query = new Query(QueryBuilders.matchQuery("field", "value"), emptyList(), 0);

            List<CaseDetails> cases = List.of(CaseDetails.builder().id(1L).build());
            SearchResult searchResult = SearchResult.builder().cases(cases).build();

            when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query.toString()))
                .thenReturn(searchResult);

            List<CaseDetails> casesFound = service.searchCases(query).getCases();

            assertThat(casesFound).isEqualTo(cases);
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query.toString());
            verify(idamClient).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        }
    }

    @Nested
    class GetCase {

        @Test
        void shouldReturnCase_WhenInvoked() {
            CaseDetails expectedCaseDetails = CaseDetails.builder().id(1L).build();
            when(coreCaseDataApi.getCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "1"))
                .thenReturn(expectedCaseDetails);

            CaseDetails caseDetails = service.getCase(1L);

            assertThat(caseDetails).isEqualTo(expectedCaseDetails);
            verify(coreCaseDataApi).getCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "1");
            verify(idamClient).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        }
    }
}
