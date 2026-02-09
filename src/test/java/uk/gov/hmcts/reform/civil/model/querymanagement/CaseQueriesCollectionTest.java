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
        CaseMessage queryMessage = new CaseMessage();
        queryMessage.setId("query-id");
        queryMessage.setIsHearingRelated(YES);
        queryMessage.setCreatedOn(now);
        CaseMessage oldQueryMessage = new CaseMessage();
        oldQueryMessage.setId("old-query-id");
        oldQueryMessage.setIsHearingRelated(NO);
        oldQueryMessage.setCreatedOn(now.minusMinutes(10));
        CaseQueriesCollection caseQueries = buildCaseQueries(
            "John Doe",
            "applicant-solicitor",
            List.of(
                Element.<CaseMessage>builder()
                    .id(UUID.randomUUID())
                    .value(queryMessage).build(),
                Element.<CaseMessage>builder()
                    .id(UUID.randomUUID())
                    .value(oldQueryMessage).build()
            )
        );

        CaseMessage latest = caseQueries.latest();

        assertEquals("query-id", latest.getId());
        assertEquals(now, latest.getCreatedOn());
    }

    @Test
    void shouldReturnNullWhenCaseMessagesIsEmpty() {
        CaseQueriesCollection caseQueries = buildCaseQueries("John Doe", "applicant-solicitor", List.of());

        CaseMessage latest = caseQueries.latest();

        assertNull(latest);
    }

    @Test
    void shouldReturnNullWhenCaseMessagesIsNull() {
        CaseQueriesCollection caseQueries = buildCaseQueries("John Doe", "applicant-solicitor", null);

        CaseMessage latest = caseQueries.latest();

        assertNull(latest);
    }

    @Test
    void shouldReturnTrue_whenCaseQueriesCollectionIsSame() {
        CaseQueriesCollection caseQueries = buildCaseQueries("John Doe", "applicant-solicitor", null);

        CaseQueriesCollection sameCaseQueries = buildCaseQueries("John Doe", "applicant-solicitor", null);

        boolean result = caseQueries.isSame(sameCaseQueries);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenCaseQueriesCollectionIsNull() {
        CaseQueriesCollection caseQueries = buildCaseQueries("John Doe", "applicant-solicitor", null);

        boolean result = caseQueries.isSame(null);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenCaseQueriesCollectionIsDifferent() {
        CaseQueriesCollection caseQueries = buildCaseQueries("John Doe", "applicant-solicitor", null);

        CaseQueriesCollection differentCaseQueries = buildCaseQueries("Jane Doe", "respondent-solicitor", null);

        boolean result = caseQueries.isSame(differentCaseQueries);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_collectionHasNoQueriesAwaitingAResponse() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseMessage queryMessage = new CaseMessage();
        queryMessage.setId("query-id");
        queryMessage.setIsHearingRelated(YES);
        queryMessage.setCreatedOn(now);
        CaseMessage responseMessage = new CaseMessage();
        responseMessage.setId("response-id");
        responseMessage.setIsHearingRelated(NO);
        responseMessage.setCreatedOn(now.plusHours(3));
        responseMessage.setParentId("query-id");
        CaseQueriesCollection caseQueries = buildCaseQueries(
            "John Doe",
            "applicant-solicitor",
            List.of(
                Element.<CaseMessage>builder()
                    .id(UUID.randomUUID())
                    .value(queryMessage).build(),
                Element.<CaseMessage>builder()
                    .id(UUID.randomUUID())
                    .value(responseMessage).build()
            )
        );

        assertFalse(caseQueries.hasAQueryAwaitingResponse());
    }

    @Test
    void shouldReturnTrue_whenCollectionHasQueryAwaitingResponse() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseMessage queryMessage = new CaseMessage();
        queryMessage.setId("query-id");
        queryMessage.setIsHearingRelated(YES);
        queryMessage.setCreatedOn(now);
        CaseMessage responseMessage = new CaseMessage();
        responseMessage.setId("response-id");
        responseMessage.setIsHearingRelated(NO);
        responseMessage.setCreatedOn(now.plusHours(3));
        responseMessage.setParentId("query-id");
        CaseMessage followUpMessage = new CaseMessage();
        followUpMessage.setId("followup-id");
        followUpMessage.setIsHearingRelated(NO);
        followUpMessage.setCreatedOn(now.plusHours(5));
        followUpMessage.setParentId("query-id");
        CaseQueriesCollection caseQueries = buildCaseQueries(
            "John Doe",
            "applicant-solicitor",
            List.of(
                Element.<CaseMessage>builder()
                    .id(UUID.randomUUID())
                    .value(queryMessage).build(),
                Element.<CaseMessage>builder()
                    .id(UUID.randomUUID())
                    .value(responseMessage).build(),
                Element.<CaseMessage>builder()
                    .id(UUID.randomUUID())
                    .value(followUpMessage).build()
            )
        );

        assertTrue(caseQueries.hasAQueryAwaitingResponse());
    }

    @Test
    void shouldReturnCorrectThreadForInitialMessage() {
        OffsetDateTime now = OffsetDateTime.of(LocalDateTime.of(2025, 3, 1, 7, 0, 0), ZoneOffset.UTC);
        String rootMessageId = "root-msg-id";
        String childMessageId = "child-msg-id";

        CaseMessage rootMessage = new CaseMessage();
        rootMessage.setId(rootMessageId);
        rootMessage.setCreatedOn(now);
        CaseMessage childMessage = new CaseMessage();
        childMessage.setId(childMessageId);
        childMessage.setParentId(rootMessageId);
        childMessage.setCreatedOn(now.plusMinutes(5));
        String otherRootId = "other-root-id";
        CaseMessage otherRootMessage = new CaseMessage();
        otherRootMessage.setId(otherRootId);
        otherRootMessage.setCreatedOn(now.minusDays(1));

        CaseQueriesCollection caseQueries = buildCaseQueries(null, null, List.of(
            Element.<CaseMessage>builder().id(UUID.randomUUID()).value(rootMessage).build(),
            Element.<CaseMessage>builder().id(UUID.randomUUID()).value(childMessage).build(),
            Element.<CaseMessage>builder().id(UUID.randomUUID()).value(otherRootMessage).build()
        ));

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

        CaseMessage rootMessage = new CaseMessage();
        rootMessage.setId(rootMessageId);
        rootMessage.setCreatedOn(now);
        CaseMessage childMessage = new CaseMessage();
        childMessage.setId(childMessageId);
        childMessage.setParentId(rootMessageId);
        childMessage.setCreatedOn(now.plusMinutes(5));
        String otherRootId = "other-root-id";
        CaseMessage otherRootMessage = new CaseMessage();
        otherRootMessage.setId(otherRootId);
        otherRootMessage.setCreatedOn(now.minusDays(1));

        CaseQueriesCollection caseQueries = buildCaseQueries(null, null, List.of(
            Element.<CaseMessage>builder().id(UUID.randomUUID()).value(rootMessage).build(),
            Element.<CaseMessage>builder().id(UUID.randomUUID()).value(childMessage).build(),
            Element.<CaseMessage>builder().id(UUID.randomUUID()).value(otherRootMessage).build()
        ));

        List<Element<CaseMessage>> thread = caseQueries.messageThread(childMessage);

        assertNotNull(thread);
        assertEquals(2, thread.size()); // Expecting root and child, as per messageThread(String) logic
        assertEquals(rootMessageId, thread.get(0).getValue().getId());
        assertEquals(childMessageId, thread.get(1).getValue().getId());
    }

    @Test
    void shouldReturnEmptyList_whenCaseMessagesCollectionIsEmpty() {
        CaseQueriesCollection caseQueries = buildCaseQueries(null, null, Collections.emptyList());

        CaseMessage message = new CaseMessage();
        message.setId("some-id");
        message.setCreatedOn(OffsetDateTime.now());
        List<Element<CaseMessage>> thread = caseQueries.messageThread(message);

        assertNotNull(thread);
        assertEquals(0, thread.size());
        assertTrue(thread.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenCaseMessagesCollectionIsNull() {
        CaseQueriesCollection caseQueries = buildCaseQueries(null, null, null);

        CaseMessage message = new CaseMessage();
        message.setId("some-id");
        message.setCreatedOn(OffsetDateTime.now());
        List<Element<CaseMessage>> thread = caseQueries.messageThread(message);

        assertNotNull(thread);
        assertEquals(0, thread.size());
        assertTrue(thread.isEmpty());
    }

    private CaseQueriesCollection buildCaseQueries(String partyName,
                                                   String roleOnCase,
                                                   List<Element<CaseMessage>> messages) {
        CaseQueriesCollection caseQueriesCollection = new CaseQueriesCollection();
        caseQueriesCollection.setPartyName(partyName);
        caseQueriesCollection.setRoleOnCase(roleOnCase);
        caseQueriesCollection.setCaseMessages(messages);
        return caseQueriesCollection;
    }

}
