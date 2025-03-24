package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCaseDataServiceTest {

    @InjectMocks
    private CoreCaseDataService service;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final String CASE_TYPE = "CIVIL";
    private static final String CMC_CASE_TYPE = "MoneyClaimCase";
    private static final String GENERAL_APPLICATION_CASE_TYPE = "GENERALAPPLICATION";
    private static final Integer RETURNED_NUMBER_OF_CASES = 10;

    @BeforeEach
    void init() {
        clearInvocations(authTokenGenerator, userService);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
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
            when(userService.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);

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

        @Test
        void shouldSetEventSummaryAndDescription_WhenCalled() {
            service.triggerEvent(Long.valueOf(CASE_ID), CaseEvent.valueOf(EVENT_ID), Map.of(), "Summary", "Desc");

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
                eq(CaseDataContent.builder()
                       .data(caseDetails.getData())
                       .event(Event.builder()
                                  .id(EVENT_ID)
                                  .summary("Summary")
                                  .description("Desc")
                                  .build())
                       .eventToken(EVENT_TOKEN)
                       .build())
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
    class TriggerGeneralApplicationEvent {

        private static final String EVENT_ID = "APPLICATION_PROCEEDS_IN_HERITAGE";
        private static final String JURISDICTION = "CIVIL";
        private static final String EVENT_TOKEN = "eventToken";
        private static final String CASE_ID = "1";
        private static final String USER_ID = "User1";
        private final CaseData caseData = new GeneralApplicationDetailsBuilder().getTriggerGeneralApplicationTestData();
        private final CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

        @BeforeEach
        void setUp() {
            when(userService.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);

            when(coreCaseDataApi.startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION,
                                                         GENERAL_APPLICATION_CASE_TYPE, CASE_ID, EVENT_ID
            )).thenReturn(buildStartEventResponse());

            when(coreCaseDataApi.submitEventForCaseWorker(
                     eq(USER_AUTH_TOKEN),
                     eq(SERVICE_AUTH_TOKEN),
                     eq(USER_ID),
                     eq(JURISDICTION),
                     eq(GENERAL_APPLICATION_CASE_TYPE),
                     eq(CASE_ID),
                     anyBoolean(),
                     any(CaseDataContent.class)
                 )
            ).thenReturn(caseDetails);
        }

        @Test
        void shouldStartAndSubmitEvent_WhenCalled() {
            service.triggerGeneralApplicationEvent(Long.valueOf(CASE_ID), CaseEvent.valueOf(EVENT_ID));

            verify(coreCaseDataApi).startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION,
                                                            GENERAL_APPLICATION_CASE_TYPE, CASE_ID, EVENT_ID
            );
            verify(coreCaseDataApi).submitEventForCaseWorker(
                eq(USER_AUTH_TOKEN),
                eq(SERVICE_AUTH_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION),
                eq(GENERAL_APPLICATION_CASE_TYPE),
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

        @BeforeEach
        void setUp() {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        }

        @Test
        void shouldReturnCases_WhenSearchingCasesAsSystemUpdateUser() {
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);

            Query query = new Query(QueryBuilders.matchQuery("field", "value"), emptyList(), 0);

            List<CaseDetails> cases = List.of(CaseDetails.builder().id(1L).build());
            SearchResult searchResult = SearchResult.builder().cases(cases).build();

            when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query.toString()))
                .thenReturn(searchResult);

            List<CaseDetails> casesFound = service.searchCases(query).getCases();

            assertThat(casesFound).isEqualTo(cases);
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query.toString());
            verify(userService).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        }

        @Test
        void shouldReturnClaimantCaseList_WhenCCDClaimsForLipClaimant() {
            List<CaseDetails> cases = List.of(CaseDetails.builder().id(1L).build());
            SearchResult searchResult = SearchResult.builder().cases(cases).build();
            UserDetails userDetails = UserDetails.builder().email("someemail@email.com").build();
            given(userService.getUserDetails(anyString())).willReturn(userDetails);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery("data.claimantUserDetails.email", userDetails.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();

            when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query))
                .thenReturn(searchResult);
            var claimsResult = service.getCCDClaimsForLipClaimant(USER_AUTH_TOKEN, 0);
            assertThat(claimsResult).isEqualTo(searchResult);
        }

        @Test
        void shouldReturnDefendantCaseList_WhenCCDClaimsForLipDefendant() {
            List<CaseDetails> cases = List.of(CaseDetails.builder().id(1L).build());
            SearchResult searchResult = SearchResult.builder().cases(cases).build();
            UserDetails userDetails = UserDetails.builder().email("someemail@email.com").build();
            given(userService.getUserDetails(anyString())).willReturn(userDetails);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery("data.defendantUserDetails.email", userDetails.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();

            when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query))
                .thenReturn(searchResult);
            var claimsResult = service.getCCDClaimsForLipDefendant(USER_AUTH_TOKEN, 0);
            assertThat(claimsResult).isEqualTo(searchResult);
        }

        @Test
        void shouldSearchCasesByDefendantUser_whenLipVLipEnabled() {
            UserDetails userDetails = UserDetails.builder().email("someemail@email.com").build();
            given(userService.getUserDetails(anyString())).willReturn(userDetails);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery("data.defendantUserDetails.email", userDetails.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();
            service.getCCDDataBasedOnIndex(USER_AUTH_TOKEN, 0, "data.defendantUserDetails.email");
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query);
        }

        @Test
        void shouldSearchCasesByClaimantUser_whenLipVLipEnabled() {
            UserDetails userDetails = UserDetails.builder().email("someemail@email.com").build();
            given(userService.getUserDetails(anyString())).willReturn(userDetails);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery("data.claimantUserDetails.email", userDetails.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();
            service.getCCDDataBasedOnIndex(USER_AUTH_TOKEN, 0, "data.claimantUserDetails.email");
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query);
        }

    }

    @Nested
    class SearchCMCCases {

        @BeforeEach
        void setUp() {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        }

        @Test
        void shouldReturnCases_WhenSearchingCasesAsSystemUpdateUser() {
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);

            Query query = new Query(QueryBuilders.matchQuery("previousServiceCaseReference", "000MC001"), emptyList(), 0);

            List<CaseDetails> cases = List.of(CaseDetails.builder().id(1L).build());
            SearchResult searchResult = SearchResult.builder().cases(cases).build();

            when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CMC_CASE_TYPE, query.toString()))
                .thenReturn(searchResult);

            List<CaseDetails> casesFound = service.searchCMCCases(query).getCases();

            assertThat(casesFound).isEqualTo(cases);
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CMC_CASE_TYPE, query.toString());
            verify(userService).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        }
    }

    @Nested
    class GetCase {

        @BeforeEach
        void setUp() {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);
        }

        @Test
        void shouldReturnCase_WhenInvoked() {
            CaseDetails expectedCaseDetails = CaseDetails.builder().id(1L).build();
            when(coreCaseDataApi.getCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "1"))
                .thenReturn(expectedCaseDetails);

            CaseDetails caseDetails = service.getCase(1L);

            assertThat(caseDetails).isEqualTo(expectedCaseDetails);
            verify(coreCaseDataApi).getCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "1");
            verify(userService).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        }
    }

    @Nested
    class GetSupplementaryData {
        private static final String USER_ID = "User1";

        @BeforeEach
        void setUp() {
            when(userService.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);
        }

        @Test
        void shouldReturnCase_WhenInvoked1() {
            service.setSupplementaryData(Long.valueOf("1"), supplementaryData());

            verify(coreCaseDataApi).submitSupplementaryData(
                USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "1", supplementaryData()
            );
        }
    }

    @Nested
    class GetAgreedDeadlineResponseDate {

        @BeforeEach
        void setUp() {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        }

        @Test
        void shouldReturnRespondentSolicitor1AgreedDeadlineExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
            CaseDetails caseDetails = CaseDetails.builder().build();
            caseDetails.setData(caseData.toMap(objectMapper));
            when(service.getCase(1L, "AUTH")).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            LocalDate agreedDeadlineExpected = LocalDate.now().plusDays(14);
            LocalDate agreedDeadline = service.getAgreedDeadlineResponseDate(1L, "AUTH");
            assertThat(agreedDeadline).isEqualTo(agreedDeadlineExpected);
        }

        @Test
        void shouldReturnUndefined_RespondentSolicitor1AgreedDeadlineExtension() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CaseDetails caseDetails = CaseDetails.builder().build();
            caseDetails.setData(caseData.toMap(objectMapper));
            when(service.getCase(1L, "AUTH")).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            LocalDate agreedDeadline = service.getAgreedDeadlineResponseDate(1L, "AUTH");
            assertThat(agreedDeadline).isNull();
        }
    }

    private Map<String, Map<String, Map<String, Object>>> supplementaryData() {
        Map<String, Object> hmctsServiceIdMap = new HashMap<>();
        hmctsServiceIdMap.put("HMCTSServiceId", "AAA6");

        Map<String, Map<String, Object>> supplementaryDataRequestMap = new HashMap<>();
        supplementaryDataRequestMap.put("$set", hmctsServiceIdMap);

        Map<String, Map<String, Map<String, Object>>> supplementaryDataUpdates = new HashMap<>();
        supplementaryDataUpdates.put("supplementary_data_updates", supplementaryDataRequestMap);

        return supplementaryDataUpdates;
    }
}
