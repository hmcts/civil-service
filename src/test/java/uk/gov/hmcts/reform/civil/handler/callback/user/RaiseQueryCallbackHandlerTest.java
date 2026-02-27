package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRaiseQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RaiseQueryCallbackHandler.INVALID_CASE_STATE_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RaiseQueryCallbackHandler.QM_NOT_ALLOWED_ERROR;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class RaiseQueryCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String USER_ID = "UserId";
    private static final Long CASE_ID = Long.parseLong("1234123412341234");
    private static final String QUERY_ID = "QueryId";
    private static final OffsetDateTime NOW = OffsetDateTime.of(LocalDateTime.of(2025, 3, 1, 7, 0, 0), ZoneOffset.UTC);

    @InjectMocks
    private RaiseQueryCallbackHandler handler;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private UserService userService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private AssignCategoryId assignCategoryId;

    @InjectMocks
    private ObjectMapper objectMapper;

    @Test
    public void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(queryManagementRaiseQuery);
    }

    @BeforeEach
    void setupTest() {
        objectMapper.registerModule(new JavaTimeModule());
        handler = new RaiseQueryCallbackHandler(
            objectMapper, userService, coreCaseUserService, assignCategoryId,
            featureToggleService
        );
    }

    @Nested
    class AboutToStartCallback {

        @Test
        public void shouldNotReturnError_whenClaimIssued() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setCcdState(CaseState.CASE_ISSUED);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        public void shouldReturnError_whenClaimPendingIssued() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseDismissedState() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setCcdState(CaseState.CASE_DISMISSED);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseOffline() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseClosed() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setCcdState(CaseState.CLOSED);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenQmNotEnabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(QM_NOT_ALLOWED_ERROR);
        }

        @Test
        void shouldMigrateAllQueries_whenFeatureToggleIsEnabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setCcdState(CaseState.CASE_PROGRESSION);
            caseData.setQmApplicantSolicitorQueries(mockQueriesCollection("app-query-id", NOW));
            caseData.setQmRespondentSolicitor1Queries(mockQueriesCollection(
                    "res-1-query-id",
                    NOW.plusDays(1)
                ));
            caseData.setQmRespondentSolicitor2Queries(mockQueriesCollection(
                    "res-2--query-id",
                    NOW.plusDays(2)
                ));

            List<Element<CaseMessage>> expectedMessages = Stream.of(
                    caseData.getQmApplicantSolicitorQueries(),
                    caseData.getQmRespondentSolicitor1Queries(),
                    caseData.getQmRespondentSolicitor2Queries()
                )
                .flatMap(collection -> collection.getCaseMessages().stream())
                .toList();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData actualData = objectMapper.convertValue(response.getData(), CaseData.class);

            CaseQueriesCollection caseQueriesCollection = new CaseQueriesCollection();
            caseQueriesCollection.setPartyName("All queries");
            caseQueriesCollection.setCaseMessages(expectedMessages);
            assertThat(actualData.getQueries()).isEqualTo(caseQueriesCollection);
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setupTest() {
            when(userService.getUserInfo(any())).thenReturn(UserInfo.builder().uid(USER_ID).build());
        }

        @Test
        void shouldReturnConcurrencyError_whenPublicQueryManagementEnabledAndMessageThreadSizeIsEven() {
            CaseMessage latestMessage = new CaseMessage();
            latestMessage.setId(QUERY_ID);
            latestMessage.setCreatedOn(NOW);

            CaseMessage olderMessage = new CaseMessage();
            olderMessage.setId(QUERY_ID); // Same ID for the thread
            olderMessage.setCreatedOn(NOW.minusMinutes(5));

            CaseQueriesCollection caseQueriesCollection = new CaseQueriesCollection();
            caseQueriesCollection.setCaseMessages(wrapElements(latestMessage, olderMessage));

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setQueries(caseQueriesCollection);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(
                "Consecutive follow up messages are not allowed for query management.");
            assertThat(response.getData()).isNull();
        }

        @Test
        void shouldNotReturnConcurrencyError_whenMessageThreadSizeIsOdd() {
            CaseMessage latestMessage = new CaseMessage();
            latestMessage.setId(QUERY_ID);
            latestMessage.setCreatedOn(NOW);

            CaseQueriesCollection caseQueriesCollection = new CaseQueriesCollection();
            caseQueriesCollection.setCaseMessages(wrapElements(latestMessage));

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setQueries(caseQueriesCollection);

            when(userService.getUserInfo(any())).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(APPLICANTSOLICITORONE.name()));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
        }

        @Test
        void shouldClearOldQueries() {
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                RESPONDENTSOLICITORONE.name()));

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setCcdState(CaseState.CASE_PROGRESSION);
            caseData.setQmApplicantSolicitorQueries(mockQueriesCollection("app-query-id", NOW));
            caseData.setQmRespondentSolicitor1Queries(mockQueriesCollection(
                    "res-1-query-id",
                    NOW.plusDays(1)
                ));
            caseData.setQmRespondentSolicitor2Queries(mockQueriesCollection(
                    "res-2--query-id",
                    NOW.plusDays(2)
                ));
            caseData.setQueries(mockQueriesCollection(
                    "query-id",
                    "All queries",
                    NOW.plusDays(5)
                ));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData actualData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(actualData.getQmApplicantSolicitorQueries()).isEqualTo(null);
            assertThat(actualData.getQmRespondentSolicitor1Queries()).isEqualTo(null);
            assertThat(actualData.getQmRespondentSolicitor2Queries()).isEqualTo(null);
            assertThat(actualData.getQueries()).isEqualTo(caseData.getQueries());
        }

        @Test
        void shouldStoreCreatorRoleOnLatestMessage() {
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                APPLICANTSOLICITORONE.getFormattedName()));

            CaseMessage latestMessage = new CaseMessage();
            latestMessage.setId(QUERY_ID);
            latestMessage.setCreatedOn(NOW);
            CaseQueriesCollection caseQueriesCollection = new CaseQueriesCollection();
            caseQueriesCollection.setCaseMessages(wrapElements(latestMessage));

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setQueries(caseQueriesCollection);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getQueries().latest().getCreatedByCaseRole())
                .isEqualTo(APPLICANTSOLICITORONE.getFormattedName());
        }
    }

    private CaseQueriesCollection mockQueriesCollection(String queryId, String partyName, OffsetDateTime latestDate) {
        CaseMessage caseMessage1 = new CaseMessage();
        caseMessage1.setId(queryId);
        caseMessage1.setIsHearingRelated(YES);
        caseMessage1.setCreatedOn(latestDate);
        Element<CaseMessage> element1 = new Element<>();
        element1.setId(UUID.randomUUID());
        element1.setValue(caseMessage1);

        CaseMessage caseMessage2 = new CaseMessage();
        caseMessage2.setId("old-query-id");
        caseMessage2.setIsHearingRelated(NO);
        caseMessage2.setCreatedOn(latestDate.minusMinutes(10));
        Element<CaseMessage> element2 = new Element<>();
        element2.setId(UUID.randomUUID());
        element2.setValue(caseMessage2);

        CaseQueriesCollection collection = new CaseQueriesCollection();
        collection.setPartyName(partyName);
        collection.setRoleOnCase("roleOnCase");
        collection.setCaseMessages(List.of(element1, element2));
        return collection;
    }

    private CaseQueriesCollection mockQueriesCollection(String queryId, OffsetDateTime latestDate) {
        return mockQueriesCollection(queryId, "partyName", latestDate);
    }
}
