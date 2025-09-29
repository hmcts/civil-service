package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRespondQuery;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class RespondQueryCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final Long CASE_ID = Long.parseLong("1234123412341234");
    private static final String QUERY_ID = "QueryId";
    private static final OffsetDateTime NOW = OffsetDateTime.of(LocalDateTime.of(2025, 3, 1, 7, 0, 0), ZoneOffset.UTC);

    @InjectMocks
    private RespondQueryCallbackHandler handler;

    @InjectMocks
    private AssignCategoryId assignCategoryId;

    @InjectMocks
    private ObjectMapper objectMapper;

    @Test
    public void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(queryManagementRespondQuery);
    }

    @Nested
    class AboutToStartCallback {

        @BeforeEach
        void setupTest() {
            objectMapper.registerModule(new JavaTimeModule());
            handler = new RespondQueryCallbackHandler(objectMapper, assignCategoryId);
        }

        @Test
        void shouldMigrateAllQueries() {
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
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setupTest() {
            objectMapper.registerModule(new JavaTimeModule());
            handler = new RespondQueryCallbackHandler(objectMapper, assignCategoryId);
        }

        @Test
        public void shouldAssignClaimantQueryCategoryId_forCaseWorker() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .queries(mockQueriesCollection(QUERY_ID, NOW))
                .build();

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


