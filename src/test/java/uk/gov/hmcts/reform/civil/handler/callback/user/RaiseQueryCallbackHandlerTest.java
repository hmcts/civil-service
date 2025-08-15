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
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
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
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RaiseQueryCallbackHandler.INVALID_CASE_STATE_ERROR;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
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
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_ISSUED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        public void shouldReturnError_whenClaimPendingIssued() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.PENDING_CASE_ISSUED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseDismissedState() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_DISMISSED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseOffline() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseClosed() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CLOSED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        void shouldMigrateAllQueries_whenFeatureToggleIsEnabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_PROGRESSION)
                .qmApplicantSolicitorQueries(mockQueriesCollection("app-query-id", NOW))
                .qmRespondentSolicitor1Queries(mockQueriesCollection(
                    "res-1-query-id",
                    NOW.plusDays(1)
                ))
                .qmRespondentSolicitor2Queries(mockQueriesCollection(
                    "res-2--query-id",
                    NOW.plusDays(2)
                ))
                .build();

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

            assertThat(actualData.getQueries()).isEqualTo(CaseQueriesCollection.builder()
                                                              .partyName("All queries")
                                                              .caseMessages(expectedMessages)
                                                              .build());
        }

        @Test
        void shouldNotMigrateAllQueries_whenFeatureToggleIsDisabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_PROGRESSION)
                .qmApplicantSolicitorQueries(mockQueriesCollection("app-query-id", NOW))
                .qmRespondentSolicitor1Queries(mockQueriesCollection(
                    "res-1-query-id",
                    NOW.plusDays(1)
                ))
                .qmRespondentSolicitor2Queries(mockQueriesCollection(
                    "res-2--query-id",
                    NOW.plusDays(2)
                ))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData actualData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(actualData.getQueries()).isEqualTo(null);
            assertThat(actualData.getQmApplicantSolicitorQueries()).isEqualTo(caseData.getQmApplicantSolicitorQueries());
            assertThat(actualData.getQmRespondentSolicitor1Queries()).isEqualTo(caseData.getQmRespondentSolicitor1Queries());
            assertThat(actualData.getQmRespondentSolicitor2Queries()).isEqualTo(caseData.getQmRespondentSolicitor2Queries());
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
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseMessage latestMessage = CaseMessage.builder()
                .id(QUERY_ID)
                .createdOn(NOW)
                .build();

            CaseMessage olderMessage = CaseMessage.builder()
                .id(QUERY_ID) // Same ID for the thread
                .createdOn(NOW.minusMinutes(5))
                .build();

            CaseQueriesCollection caseQueriesCollection = CaseQueriesCollection.builder()
                .caseMessages(wrapElements(latestMessage, olderMessage))
                .build();

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .queries(caseQueriesCollection)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly("Consecutive follow up messages are not allowed for query management.");
            assertThat(response.getData()).isNull();
        }

        @Test
        void shouldNotReturnConcurrencyError_whenPublicQueryManagementEnabledAndMessageThreadSizeIsOdd() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseMessage latestMessage = CaseMessage.builder()
                .id(QUERY_ID)
                .createdOn(NOW)
                .build();

            CaseQueriesCollection caseQueriesCollection = CaseQueriesCollection.builder()
                .caseMessages(wrapElements(latestMessage))
                .build();

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .queries(caseQueriesCollection)
                .build();

            when(userService.getUserInfo(any())).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(APPLICANTSOLICITORONE.name()));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
        }

        @Test
        void shouldNotReturnConcurrencyError_whenPublicQueryManagementDisabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);

            CaseMessage latestUserMessage = CaseMessage.builder()
                .id(QUERY_ID)
                .createdOn(NOW)
                .build();

            CaseQueriesCollection applicantQueries = CaseQueriesCollection.builder()
                .caseMessages(wrapElements(latestUserMessage))
                .build();

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmApplicantSolicitorQueries(applicantQueries)
                .build();

            when(userService.getUserInfo(any())).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(APPLICANTSOLICITORONE.name()));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
        }

        @Test
        public void shouldPopulateLatestQueryWithApplicantQueryData() {
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                APPLICANTSOLICITORONE.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmApplicantSolicitorQueries(mockQueriesCollection(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("queryId")
                .isEqualTo(QUERY_ID);
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("isHearingRelated")
                .isEqualTo(YES.getLabel());
        }

        @Test
        public void shouldAssignCategoryIdToLatestApplicantQuery() {
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                APPLICANTSOLICITORONE.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmApplicantSolicitorQueries(mockQueriesCollectionWithAttachments(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            List<Document> documents = unwrapElements(updatedData.getQmApplicantSolicitorQueries()
                                                          .getCaseMessages().get(0).getValue()
                                                          .getAttachments());

            assertThat(response.getErrors()).isNull();
            for (Document document : documents) {
                assertThat(document.getCategoryID()).isEqualTo(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue());
            }
        }

        @Test
        public void shouldAssignCategoryIdToLatestRespondent1Query() {
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                RESPONDENTSOLICITORONE.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmRespondentSolicitor1Queries(mockQueriesCollectionWithAttachments(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            List<Document> documents = unwrapElements(updatedData.getQmRespondentSolicitor1Queries()
                                                          .getCaseMessages().get(0).getValue()
                                                          .getAttachments());

            assertThat(response.getErrors()).isNull();
            for (Document document : documents) {
                assertThat(document.getCategoryID()).isEqualTo(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue());
            }
        }

        @Test
        public void shouldAssignCategoryIdToLatestRespondent2Query() {
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                RESPONDENTSOLICITORTWO.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmRespondentSolicitor2Queries(mockQueriesCollectionWithAttachments(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            List<Document> documents = unwrapElements(updatedData.getQmRespondentSolicitor2Queries()
                                                          .getCaseMessages().get(0).getValue()
                                                          .getAttachments());

            assertThat(response.getErrors()).isNull();
            for (Document document : documents) {
                assertThat(document.getCategoryID()).isEqualTo(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue());
            }
        }

        @Test
        public void shouldPopulateLatestQueryWithRespondent1QueryData() {
            when(coreCaseUserService.getUserCaseRoles(
                CASE_ID.toString(),
                USER_ID
            )).thenReturn(List.of(RESPONDENTSOLICITORONE.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmRespondentSolicitor1Queries(mockQueriesCollection(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("queryId")
                .isEqualTo(QUERY_ID);
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("isHearingRelated")
                .isEqualTo(YES.getLabel());
        }

        @Test
        public void shouldPopulateLatestQueryWithRespondent2QueryData() {
            when(coreCaseUserService.getUserCaseRoles(
                CASE_ID.toString(),
                USER_ID
            )).thenReturn(List.of(RESPONDENTSOLICITORTWO.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmRespondentSolicitor2Queries(mockQueriesCollection(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("queryId")
                .isEqualTo(QUERY_ID);
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("isHearingRelated")
                .isEqualTo(YES.getLabel());
        }

        @Nested
        class PartyNameUpdate {

            @Test
            public void shouldUpdateApplicantQueryCollectionPartyName_1v1() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    APPLICANTSOLICITORONE.name()));
                CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmApplicantSolicitorQueries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmApplicantSolicitorQueries().getPartyName()).isEqualTo("Claimant");
            }

            @Test
            public void shouldUpdateRespondentOneQueryCollectionPartyName_1v1() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    RESPONDENTSOLICITORONE.name()));
                CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmRespondentSolicitor1Queries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmRespondentSolicitor1Queries().getPartyName()).isEqualTo("Defendant");
            }

            @Test
            public void shouldUpdateApplicantQueryCollectionPartyName_1v2_same() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    APPLICANTSOLICITORONE.name()));
                CaseData caseData = CaseDataBuilder.builder().atState1v2SameSolicitorClaimDetailsRespondentNotifiedTimeExtension()
                    .build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmApplicantSolicitorQueries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmApplicantSolicitorQueries().getPartyName()).isEqualTo("Claimant");
            }

            @Test
            public void shouldUpdateRespondentOneQueryCollectionPartyName_1v2_same() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    RESPONDENTSOLICITORONE.name(), RESPONDENTSOLICITORTWO.name()));
                CaseData caseData = CaseDataBuilder.builder().atState1v2SameSolicitorClaimDetailsRespondentNotifiedTimeExtension()
                    .build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmRespondentSolicitor1Queries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmRespondentSolicitor1Queries().getPartyName()).isEqualTo("Defendant");
            }

            @Test
            public void shouldUpdateApplicantQueryCollectionPartyName_1v2_diff() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    APPLICANTSOLICITORONE.name()));
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension_1v2DS().build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmApplicantSolicitorQueries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmApplicantSolicitorQueries().getPartyName()).isEqualTo("Claimant");
            }

            @Test
            public void shouldUpdateRespondentQueryCollectionPartyName_1v2_diff() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    RESPONDENTSOLICITORONE.name()));
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension_1v2DS()
                    .build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmRespondentSolicitor1Queries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmRespondentSolicitor1Queries().getPartyName()).isEqualTo("Defendant 1");
            }

            @Test
            public void shouldUpdateRespondentTwoQueryCollectionPartyName_1v2_diff() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    RESPONDENTSOLICITORTWO.name()));
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension_1v2DS()
                    .build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmRespondentSolicitor2Queries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmRespondentSolicitor2Queries().getPartyName()).isEqualTo("Defendant 2");
            }

            @Test
            public void shouldUpdateApplicantQueryCollectionPartyName_2v1() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    APPLICANTSOLICITORONE.name()));
                CaseData caseData = CaseDataBuilder.builder().atStateRespondent2v1FullDefence().build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmApplicantSolicitorQueries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmApplicantSolicitorQueries().getPartyName()).isEqualTo("Claimant");
            }

            @Test
            public void shouldUpdateRespondentOneQueryCollectionPartyName_2v1() {
                when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                    RESPONDENTSOLICITORONE.name()));
                CaseData caseData = CaseDataBuilder.builder().atStateRespondent2v1FullDefence().build().toBuilder()
                    .ccdCaseReference(CASE_ID)
                    .qmRespondentSolicitor1Queries(mockQueriesCollection(QUERY_ID, NOW))
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertThat(updatedData.getQmRespondentSolicitor1Queries().getPartyName()).isEqualTo("Defendant");
            }
        }

        @Test
        void shouldClearOldQueries_whenFeatureToggleIsEnabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                RESPONDENTSOLICITORONE.name()));

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_PROGRESSION)
                .qmApplicantSolicitorQueries(mockQueriesCollection("app-query-id", NOW))
                .qmRespondentSolicitor1Queries(mockQueriesCollection(
                    "res-1-query-id",
                    NOW.plusDays(1)
                ))
                .qmRespondentSolicitor2Queries(mockQueriesCollection(
                    "res-2--query-id",
                    NOW.plusDays(2)
                ))
                .queries(mockQueriesCollection(
                    "query-id",
                    "All queries",
                    NOW.plusDays(5)
                ))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData actualData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(actualData.getQmApplicantSolicitorQueries()).isEqualTo(null);
            assertThat(actualData.getQmRespondentSolicitor1Queries()).isEqualTo(null);
            assertThat(actualData.getQmRespondentSolicitor2Queries()).isEqualTo(null);
            assertThat(actualData.getQueries()).isEqualTo(caseData.getQueries());
        }

        @Test
        void shouldNotClearOldApplicantQueries_whenFeatureToggleIsDisabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                APPLICANTSOLICITORONE.name()));

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_PROGRESSION)
                .qmApplicantSolicitorQueries(mockQueriesCollection("app-query-id", "Claimant", NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData actualData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(actualData.getQmApplicantSolicitorQueries()).isEqualTo(caseData.getQmApplicantSolicitorQueries());
        }

        @Test
        void shouldNotClearOldRespondent1Queries_whenFeatureToggleIsDisabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                RESPONDENTSOLICITORONE.name()));

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_PROGRESSION)
                .qmRespondentSolicitor1Queries(mockQueriesCollection("res-1-query-id", "Defendant", NOW.plusDays(1)))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData actualData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(actualData.getQmRespondentSolicitor1Queries()).isEqualTo(caseData.getQmRespondentSolicitor1Queries());
        }

        @Test
        void shouldNotClearOldRespondent2Queries_whenFeatureToggleIsDisabled() {
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                RESPONDENTSOLICITORTWO.name()));

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_PROGRESSION)
                .qmRespondentSolicitor2Queries(mockQueriesCollection("res-2--query-id", "Defendant 2", NOW.plusDays(2)))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData actualData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(actualData.getQmRespondentSolicitor2Queries()).isEqualTo(caseData.getQmRespondentSolicitor2Queries());
        }
    }

    private CaseQueriesCollection mockQueriesCollection(String queryId, String partyName, OffsetDateTime latestDate) {
        return CaseQueriesCollection.builder()
            .partyName(partyName)
            .roleOnCase("roleOnCase")
            .caseMessages(
                List.of(
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id(queryId)
                                .isHearingRelated(YES)
                                .createdOn(latestDate)
                                .build()).build(),
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("old-query-id")
                                .isHearingRelated(NO)
                                .createdOn(latestDate.minusMinutes(10))
                                .build()).build()
                ))
            .build();
    }

    private CaseQueriesCollection mockQueriesCollection(String queryId, OffsetDateTime latestDate) {
        return mockQueriesCollection(queryId, "partyName", latestDate);
    }

    private CaseQueriesCollection mockQueriesCollectionWithAttachments(String queryId, OffsetDateTime latestDate) {
        return CaseQueriesCollection.builder()
            .partyName("partyName")
            .roleOnCase("roleOnCase")
            .caseMessages(
                List.of(
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id(queryId)
                                .isHearingRelated(YES)
                                .createdOn(latestDate)
                                .attachments(wrapElements(
                                    Document.builder()
                                        .documentFileName("file1")
                                        .build(),
                                    Document.builder()
                                        .documentFileName("file2")
                                        .build()
                                ))
                                .build()).build()))
            .build();
    }
}
