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
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRespondQuery;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class RespondQueryCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String USER_ID = "UserId";
    private static final Long CASE_ID = Long.parseLong("1234123412341234");
    private static final String QUERY_ID = "QueryId";
    private static final OffsetDateTime NOW = OffsetDateTime.of(LocalDateTime.of(2025, 3, 1, 7, 0, 0), ZoneOffset.UTC);

    @InjectMocks
    private RespondQueryCallbackHandler handler;

    @InjectMocks
    private AssignCategoryId assignCategoryId;

    @InjectMocks
    private ObjectMapper objectMapper;

    @Mock
    private FeatureToggleService featuretoggleService;

    @Test
    public void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(queryManagementRespondQuery);
    }

    @Nested
    class AboutToStartCallback {

        @BeforeEach
        void setupTest() {
            objectMapper.registerModule(new JavaTimeModule());
            handler = new RespondQueryCallbackHandler(objectMapper, assignCategoryId, featuretoggleService);
        }

        @Test
        void shouldMigrateAllQueries_whenFeatureToggleIsEnabled() {
            when(featuretoggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
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
            when(featuretoggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
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
            objectMapper.registerModule(new JavaTimeModule());
            handler = new RespondQueryCallbackHandler(
                objectMapper, assignCategoryId, featuretoggleService
            );
        }

        @Test
        public void shouldAssignClaimantQueryCategoryId_forCaseWorker() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .queries(mockQueriesCollection(QUERY_ID, NOW))
                .build();
            when(featuretoggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            List<Document> documents = unwrapElements(updatedData.getQueries()
                                                          .getCaseMessages().get(0).getValue()
                                                          .getAttachments());

            assertThat(response.getErrors()).isNull();
            for (Document document : documents) {
                assertThat(document.getCategoryID()).isEqualTo(DocCategory.CASEWORKER_QUERY_DOCUMENT_ATTACHMENTS.getValue());
            }
        }

        @Test
        public void shouldAssignClaimantQueryCategoryId_forClaimantQueryResponse() {
            when(featuretoggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmApplicantSolicitorQueries(mockQueriesCollection(QUERY_ID, NOW))
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
        public void shouldAssignClaimantQueryCategoryId_forDefendant1QueryResponse() {
            when(featuretoggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmRespondentSolicitor1Queries(mockQueriesCollection(QUERY_ID, NOW))
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
        public void shouldAssignClaimantQueryCategoryId_forDefendant2QueryResponse() {
            when(featuretoggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmRespondentSolicitor2Queries(mockQueriesCollection(QUERY_ID, NOW))
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
        void shouldClearOldQueries_whenFeatureToggleIsEnabled() {
            when(featuretoggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
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
        void shouldNotClearOldQueries_whenFeatureToggleIsDisabled() {
            when(featuretoggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(false);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmApplicantSolicitorQueries(mockQueriesCollection("app-query-id", NOW))
                .qmRespondentSolicitor1Queries(mockQueriesCollection(
                    "res-1-query-id",
                    NOW.plusDays(1)
                ))
                .qmRespondentSolicitor2Queries(mockQueriesCollection(
                    "res-2--query-id",
                    NOW.plusDays(2)
                )).build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData actualData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(actualData.getQmApplicantSolicitorQueries()).isEqualTo(caseData.getQmApplicantSolicitorQueries());
            assertThat(actualData.getQmRespondentSolicitor1Queries()).isEqualTo(caseData.getQmRespondentSolicitor1Queries());
            assertThat(actualData.getQmRespondentSolicitor2Queries()).isEqualTo(caseData.getQmRespondentSolicitor2Queries());
        }
    }

    private CaseQueriesCollection mockQueriesCollection(String queryId, OffsetDateTime latestDate) {
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
                                .parentId("parent-id")
                                .isHearingRelated(NO)
                                .createdOn(latestDate)
                                .attachments(wrapElements(
                                    Document.builder()
                                        .documentFileName("file1")
                                        .build(),
                                    Document.builder()
                                        .documentFileName("file2")
                                        .build()
                                ))
                                .build()).build(),
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("parent-id")
                                .isHearingRelated(NO)
                                .createdOn(latestDate.minusMinutes(10))
                                .build()).build()
                ))
            .build();
    }

}


