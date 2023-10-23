package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
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
import uk.gov.hmcts.reform.idam.client.IdamClient;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    CoreCaseDataService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class CoreCaseDataServiceTest {

    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final String CASE_TYPE = "CIVIL";
    public static final String GENERALAPPLICATION_CASE_TYPE = "GENERALAPPLICATION";
    private static final Integer RETURNED_NUMBER_OF_CASES = 10;

    @MockBean
    private SystemUpdateUserConfiguration userConfig;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private IdamClient idamClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CoreCaseDataService service;

    @BeforeEach
    void init() {
        clearInvocations(authTokenGenerator);
        clearInvocations(userService);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(userService.getAccessToken(
            userConfig.getUserName(),
            userConfig.getPassword()
        ))
            .thenReturn(USER_AUTH_TOKEN);
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
    class TriggerGeneralApplicationEvent {

        private static final String EVENT_ID = "APPLICATION_PROCEEDS_IN_HERITAGE";
        private static final String JURISDICTION = "CIVIL";
        private static final String EVENT_TOKEN = "eventToken";
        private static final String CASE_ID = "1";
        private static final String USER_ID = "User1";
        private final CaseData caseData = new GeneralApplicationDetailsBuilder()
            .getTriggerGeneralApplicationTestData();
        private final CaseDetails caseDetails = CaseDetailsBuilder.builder()
            .data(caseData)
            .build();

        @BeforeEach
        void setUp() {
            when(userService.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());

            when(coreCaseDataApi.startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION,
                                                         GENERALAPPLICATION_CASE_TYPE, CASE_ID, EVENT_ID
            )).thenReturn(buildStartEventResponse());

            when(coreCaseDataApi.submitEventForCaseWorker(
                     eq(USER_AUTH_TOKEN),
                     eq(SERVICE_AUTH_TOKEN),
                     eq(USER_ID),
                     eq(JURISDICTION),
                     eq(GENERALAPPLICATION_CASE_TYPE),
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
                                                            GENERALAPPLICATION_CASE_TYPE, CASE_ID, EVENT_ID
            );
            verify(coreCaseDataApi).submitEventForCaseWorker(
                eq(USER_AUTH_TOKEN),
                eq(SERVICE_AUTH_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION),
                eq(GENERALAPPLICATION_CASE_TYPE),
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
            verify(userService).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        }

        @Test
        void shouldReturnClaimantCaseList_WhenCCDClaimsForLipClaimant() {
            List<CaseDetails> cases = List.of(CaseDetails.builder().id(1L).build());
            SearchResult searchResult = SearchResult.builder().cases(cases).build();
            UserDetails userDetails = UserDetails.builder().email("someemail@email.com").build();
            given(featureToggleService.isLipVLipEnabled()).willReturn(true);
            given(idamClient.getUserDetails(anyString())).willReturn(userDetails);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery("data.claimantUserDetails.email", userDetails.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();

            when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query.toString()))
                .thenReturn(searchResult);
            var claimsResult = service.getCCDClaimsForLipClaimant(USER_AUTH_TOKEN, 0);
            assertThat(claimsResult).isEqualTo(searchResult);

        }

        @Test
        void shouldReturnDefendantCaseList_WhenCCDClaimsForLipDefendant() {
            List<CaseDetails> cases = List.of(CaseDetails.builder().id(1L).build());
            SearchResult searchResult = SearchResult.builder().cases(cases).build();
            UserDetails userDetails = UserDetails.builder().email("someemail@email.com").build();
            given(featureToggleService.isLipVLipEnabled()).willReturn(true);
            given(idamClient.getUserDetails(anyString())).willReturn(userDetails);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery("data.defendantUserDetails.email", userDetails.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();

            when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query.toString()))
                .thenReturn(searchResult);
            var claimsResult = service.getCCDClaimsForLipDefendant(USER_AUTH_TOKEN, 0);
            assertThat(claimsResult).isEqualTo(searchResult);

        }

        @Test
        void shouldSearchCasesByDefendantUser_whenLipVLipEnabled() {
            //Given
            UserDetails userDetails = UserDetails.builder().email("someemail@email.com").build();
            given(featureToggleService.isLipVLipEnabled()).willReturn(true);
            given(idamClient.getUserDetails(anyString())).willReturn(userDetails);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery("data.defendantUserDetails.email", userDetails.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();
            //When
            service.getCCDDataBasedOnIndex(USER_AUTH_TOKEN, 0, "data.defendantUserDetails.email");
            //Then
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query);
        }

        @Test
        void shouldSearchCasesByClaimantUser_whenLipVLipEnabled() {
            //Given
            UserDetails userDetails = UserDetails.builder().email("someemail@email.com").build();
            given(featureToggleService.isLipVLipEnabled()).willReturn(true);
            given(idamClient.getUserDetails(anyString())).willReturn(userDetails);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery("data.claimantUserDetails.email", userDetails.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();
            //When
            service.getCCDDataBasedOnIndex(USER_AUTH_TOKEN, 0, "data.claimantUserDetails.email");
            //Then
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query);
        }

        @Test
        void shouldSearchAllCasesForUser_whenLipVLipDisabled() {
            //Given
            given(featureToggleService.isLipVLipEnabled()).willReturn(false);
            String query = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .sort("data.submittedDate", SortOrder.DESC)
                .from(0)
                .size(RETURNED_NUMBER_OF_CASES).toString();
            //When
            service.getCCDDataBasedOnIndex(USER_AUTH_TOKEN, 0, "data.defendantUserDetails.email");
            //Then
            verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, query);
            verify(idamClient, never()).getUserDetails(USER_AUTH_TOKEN);
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
            verify(userService).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        }
    }

    @Nested
    class GetSupplementaryData {
        private static final String USER_ID = "User1";

        @Test
        void shouldReturnCase_WhenInvoked1() {
            when(userService.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
            service.setSupplementaryData(Long.valueOf("1"), supplementaryData());

            verify(coreCaseDataApi).submitSupplementaryData(USER_AUTH_TOKEN,
                                                            SERVICE_AUTH_TOKEN, "1", supplementaryData()
            );
        }
    }

    @Nested
    class GetAgreedDeadlineResponseDate {

        @Test
        void shouldReturnRespondentSolicitor1AgreedDeadlineExtension() {
            //Given
            LocalDate agreedDeadlineExpected = LocalDate.now().plusDays(14);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
            CaseDetails caseDetails = CaseDetails.builder().build();
            caseDetails.setData(caseData.toMap(objectMapper));
            when(service.getCase(1L, "AUTH"))
                .thenReturn(caseDetails);
            //When
            LocalDate agreedDeadline = service.getAgreedDeadlineResponseDate(1L, "AUTH");
            //Then
            assertThat(agreedDeadline).isEqualTo(agreedDeadlineExpected);
        }

        @Test
        void shouldReturnUndefined_RespondentSolicitor1AgreedDeadlineExtension() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().build();
            CaseDetails caseDetails = CaseDetails.builder().build();
            caseDetails.setData(caseData.toMap(objectMapper));
            when(service.getCase(1L, "AUTH"))
                .thenReturn(caseDetails);
            //When
            LocalDate agreedDeadline = service.getAgreedDeadlineResponseDate(1L, "AUTH");
            //Then
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
