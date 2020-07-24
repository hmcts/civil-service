package uk.gov.hmcts.reform.unspec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.config.SystemUpdateUserConfiguration;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCaseDataServiceTest {

    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final String CASE_TYPE = "UNSPECIFIED_CLAIMS";

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CoreCaseDataService service;

    @BeforeEach
    void init() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);
    }

    @Nested
    class TriggerEvent {

        private static final String EVENT_ID = "MOVE_TO_STAYED";
        private static final String JURISDICTION = "CIVIL";
        private static final String EVENT_TOKEN = "eventToken";
        private static final long CASE_ID = 1L;
        private static final String USER_ID = "User1";

        @BeforeEach
        void setUp() {
            when(idamClient.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());

            when(coreCaseDataApi.startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION,
                CASE_TYPE, Long.toString(CASE_ID), EVENT_ID))
                .thenReturn(buildStartEventResponse());
        }

        @Test
        void shouldStartAndSubmitEvent_WhenCalled() {
            service.triggerEvent(CASE_ID, CaseEvent.valueOf(EVENT_ID));

            verify(coreCaseDataApi).startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID,
                JURISDICTION, CASE_TYPE, Long.toString(CASE_ID), EVENT_ID);
            verify(coreCaseDataApi).submitEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION,
                CASE_TYPE, Long.toString(CASE_ID), true, buildCaseDataContent());
        }

        private StartEventResponse buildStartEventResponse() {
            return StartEventResponse.builder()
                .eventId(EVENT_ID)
                .token(EVENT_TOKEN)
                .caseDetails(CaseDetails.builder()
                    .data(Map.of("data", "some data"))
                    .build())
                .build();
        }

        private CaseDataContent buildCaseDataContent() {
            return CaseDataContent.builder()
                .eventToken(EVENT_TOKEN)
                .event(Event.builder()
                    .id(EVENT_ID)
                    .build())
                .data(Map.of("data", "some data"))
                .build();
        }
    }

    @Nested
    class SearchCases {

        @Test
        void shouldReturnCases_WhenSearchingCasesAsSystemUpdateUser() {
            String query = "query";

            List<CaseDetails> cases = List.of(CaseDetails.builder().id(1L).build());
            SearchResult searchResult = SearchResult.builder().cases(cases).build();

            when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query))
                .thenReturn(searchResult);

            List<CaseDetails> casesFound = service.searchCases(query).getCases();

            assertThat(casesFound).isEqualTo(cases);
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query);
            verify(idamClient).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        }
    }
}
