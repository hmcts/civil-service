package uk.gov.hmcts.reform.civil.model.docmosis.querymanagement;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class DocumentQueryMessageTest {

    private static final LocalDate HEARING_DATE = LocalDate.of(2025, 3, 15);
    private static final OffsetDateTime DATE_QUERY_RAISED = OffsetDateTime.of(LocalDateTime.of(2025, 5, 15, 12, 0, 0), ZoneOffset.UTC);
    private static final UUID SOLICITOR_ID = UUID.randomUUID();
    private static final UUID CASEWORKER_ID = UUID.randomUUID();
    private static final String QUERY_ID = "query-id";
    private static final String PARENT_QUERY_ID = "parent-id";

    @Test
    void shouldMapFromInitialQuery() {
        CaseMessage initialQuery = new CaseMessage();
        initialQuery.setId(PARENT_QUERY_ID);
        initialQuery.setIsHearingRelated(YES);
        initialQuery.setHearingDate(HEARING_DATE);
        initialQuery.setSubject("initial query");
        initialQuery.setCreatedOn(DATE_QUERY_RAISED);
        initialQuery.setCreatedBy(SOLICITOR_ID.toString());
        initialQuery.setName("Solicitor");

        DocumentQueryMessage expected = new DocumentQueryMessage()
            .setMessageType("Query")
            .setId(PARENT_QUERY_ID)
            .setName("Solicitor")
            .setSubject("initial query")
            .setCreatedOn("15-05-2025 13:00")
            .setIsHearingRelated(YES)
            .setHearingDate("15-03-2025");

        DocumentQueryMessage actual = DocumentQueryMessage.from(initialQuery, false);
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapFromQueryResponse() {
        CaseMessage queryResponse = new CaseMessage();
        queryResponse.setId(QUERY_ID);
        queryResponse.setParentId(PARENT_QUERY_ID);
        queryResponse.setIsHearingRelated(YES);
        queryResponse.setHearingDate(HEARING_DATE);
        queryResponse.setCreatedOn(DATE_QUERY_RAISED.plusDays(1));
        queryResponse.setCreatedBy(CASEWORKER_ID.toString());
        queryResponse.setName("Hearing admin");

        DocumentQueryMessage expected = new DocumentQueryMessage()
            .setMessageType("Caseworker response")
            .setId(QUERY_ID)
            .setName("Caseworker")
            .setCreatedOn("16-05-2025 13:00");

        DocumentQueryMessage actual = DocumentQueryMessage.from(queryResponse, true);
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapFromFollowUpQuery() {
        CaseMessage queryFollowUp = new CaseMessage();
        queryFollowUp.setId(QUERY_ID);
        queryFollowUp.setParentId(PARENT_QUERY_ID);
        queryFollowUp.setIsHearingRelated(YES);
        queryFollowUp.setHearingDate(HEARING_DATE);
        queryFollowUp.setCreatedOn(DATE_QUERY_RAISED.plusDays(2));
        queryFollowUp.setCreatedBy(SOLICITOR_ID.toString());
        queryFollowUp.setName("Solicitor");

        DocumentQueryMessage expected = new DocumentQueryMessage()
            .setMessageType("Follow up")
            .setId(QUERY_ID)
            .setName("Solicitor")
            .setCreatedOn("17-05-2025 13:00");

        DocumentQueryMessage actual = DocumentQueryMessage.from(queryFollowUp, false);

        assertEquals(expected, actual);
    }
}
