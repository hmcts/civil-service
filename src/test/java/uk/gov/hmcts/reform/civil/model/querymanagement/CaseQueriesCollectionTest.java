package uk.gov.hmcts.reform.civil.model.querymanagement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class CaseQueriesCollectionTest {

    @Test
    void shouldReturnLatestCaseMessage() {
        OffsetDateTime now = OffsetDateTime.of(LocalDateTime.of(2025, 3, 1, 7, 0, 0), ZoneOffset.UTC);
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .caseMessages(
                List.of(
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("query-id")
                                .isHearingRelated(YES)
                                .createdOn(now)
                                .build()).build(),
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("old-query-id")
                                .isHearingRelated(NO)
                                .createdOn(now.minusMinutes(10))
                                .build()).build()
                ))
            .build();

        CaseMessage latest = caseQueries.latest();

        assertEquals("query-id", latest.getId());
        assertEquals(now, latest.getCreatedOn());
    }

    @Test
    void shouldReturnNullWhenCaseMessagesIsEmpty() {
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .caseMessages(List.of())
            .build();

        CaseMessage latest = caseQueries.latest();

        assertNull(latest);
    }

    @Test
    void shouldReturnNullWhenCaseMessagesIsNull() {
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .caseMessages(null)
            .build();

        CaseMessage latest = caseQueries.latest();

        assertNull(latest);
    }

    @Test
    void shouldReturnTrue_whenCaseQueriesCollectionIsSame() {
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .build();

        CaseQueriesCollection sameCaseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .build();

        boolean result = caseQueries.isSame(sameCaseQueries);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenCaseQueriesCollectionIsNull() {
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .build();

        boolean result = caseQueries.isSame(null);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenCaseQueriesCollectionIsDifferent() {
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .build();

        CaseQueriesCollection differentCaseQueries = CaseQueriesCollection.builder()
            .partyName("Jane Doe")
            .roleOnCase("respondent-solicitor")
            .build();

        boolean result = caseQueries.isSame(differentCaseQueries);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_collectionHasNoQueriesAwaitingAResponse() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .caseMessages(
                List.of(
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("query-id")
                                .isHearingRelated(YES)
                                .createdOn(now)
                                .build()).build(),
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("response-id")
                                .isHearingRelated(NO)
                                .createdOn(now.plusHours(3))
                                .parentId("query-id")
                                .build()).build()
                ))
            .build();

        assertFalse(caseQueries.hasAQueryAwaitingResponse());
    }

    @Test
    void shouldReturnTrue_whenCollectionHasQueryAwaitingResponse() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .caseMessages(
                List.of(
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("query-id")
                                .isHearingRelated(YES)
                                .createdOn(now)
                                .build()).build(),
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("response-id")
                                .isHearingRelated(NO)
                                .createdOn(now.plusHours(3))
                                .parentId("query-id")
                                .build()).build(),
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id("followup-id")
                                .isHearingRelated(NO)
                                .createdOn(now.plusHours(5))
                                .parentId("query-id")
                                .build()).build()
                ))
            .build();

        assertTrue(caseQueries.hasAQueryAwaitingResponse());
    }

    @Test
    void shouldReturnCorrectThreadForInitialMessage() {
        OffsetDateTime now = OffsetDateTime.of(LocalDateTime.of(2025, 3, 1, 7, 0, 0), ZoneOffset.UTC);
        String rootMessageId = "root-msg-id";
        String childMessageId = "child-msg-id";
        String otherRootId = "other-root-id";

        CaseMessage rootMessage = CaseMessage.builder().id(rootMessageId).createdOn(now).build();
        CaseMessage childMessage = CaseMessage.builder().id(childMessageId).parentId(rootMessageId).createdOn(now.plusMinutes(5)).build();
        CaseMessage otherRootMessage = CaseMessage.builder().id(otherRootId).createdOn(now.minusDays(1)).build();

        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .caseMessages(List.of(
                Element.<CaseMessage>builder().id(UUID.randomUUID()).value(rootMessage).build(),
                Element.<CaseMessage>builder().id(UUID.randomUUID()).value(childMessage).build(),
                Element.<CaseMessage>builder().id(UUID.randomUUID()).value(otherRootMessage).build()
            ))
            .build();

        List<Element<CaseMessage>> thread = caseQueries.messageThread(rootMessage);

        assertNotNull(thread);
        assertEquals(2, thread.size());
        assertEquals(rootMessageId, thread.get(0).getValue().getId());
        assertEquals(childMessageId, thread.get(1).getValue().getId());
    }

    @Test
    void shouldReturnCorrectThreadForChildMessage() {
        OffsetDateTime now = OffsetDateTime.of(LocalDateTime.of(2025, 3, 1, 7, 0, 0), ZoneOffset.UTC);
        String rootMessageId = "root-msg-id";
        String childMessageId = "child-msg-id";
        String otherRootId = "other-root-id";

        CaseMessage rootMessage = CaseMessage.builder().id(rootMessageId).createdOn(now).build();
        CaseMessage childMessage = CaseMessage.builder().id(childMessageId).parentId(rootMessageId).createdOn(now.plusMinutes(5)).build();
        CaseMessage otherRootMessage = CaseMessage.builder().id(otherRootId).createdOn(now.minusDays(1)).build();

        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .caseMessages(List.of(
                Element.<CaseMessage>builder().id(UUID.randomUUID()).value(rootMessage).build(),
                Element.<CaseMessage>builder().id(UUID.randomUUID()).value(childMessage).build(),
                Element.<CaseMessage>builder().id(UUID.randomUUID()).value(otherRootMessage).build()
            ))
            .build();

        List<Element<CaseMessage>> thread = caseQueries.messageThread(childMessage);

        assertNotNull(thread);
        assertEquals(2, thread.size()); // Expecting root and child, as per messageThread(String) logic
        assertEquals(rootMessageId, thread.get(0).getValue().getId());
        assertEquals(childMessageId, thread.get(1).getValue().getId());
    }

    @Test
    void shouldReturnEmptyList_whenCaseMessagesCollectionIsEmpty() {
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .caseMessages(Collections.emptyList())
            .build();

        CaseMessage message = CaseMessage.builder().id("some-id").createdOn(OffsetDateTime.now()).build();
        List<Element<CaseMessage>> thread = caseQueries.messageThread(message);

        assertNotNull(thread);
        assertEquals(0, thread.size());
        assertTrue(thread.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenCaseMessagesCollectionIsNull() {
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
            .caseMessages(null)
            .build();

        CaseMessage message = CaseMessage.builder().id("some-id").createdOn(OffsetDateTime.now()).build();
        List<Element<CaseMessage>> thread = caseQueries.messageThread(message);

        assertNotNull(thread);
        assertEquals(0, thread.size());
        assertTrue(thread.isEmpty());
    }

}
