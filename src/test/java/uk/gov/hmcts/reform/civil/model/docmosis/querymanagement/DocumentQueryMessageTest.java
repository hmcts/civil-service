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
        CaseMessage initialQuery = CaseMessage.builder()
            .id(PARENT_QUERY_ID)
            .isHearingRelated(YES)
            .hearingDate(HEARING_DATE)
            .subject("initial query")
            .createdOn(DATE_QUERY_RAISED)
            .createdBy(SOLICITOR_ID.toString())
            .name("Solicitor")
            .build();

        DocumentQueryMessage expected = DocumentQueryMessage.builder()
            .messageType("Query")
            .id(PARENT_QUERY_ID)
            .name("Solicitor")
            .subject("initial query")
            .createdOn("15-05-2025 13:00")
            .isHearingRelated(YES)
            .hearingDate("15-03-2025")
            .build();

        DocumentQueryMessage actual = DocumentQueryMessage.from(initialQuery, false);
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapFromQueryResponse() {
        CaseMessage queryResponse = CaseMessage.builder()
            .id(QUERY_ID)
            .parentId(PARENT_QUERY_ID)
            .isHearingRelated(YES)
            .hearingDate(HEARING_DATE)
            .createdOn(DATE_QUERY_RAISED.plusDays(1))
            .createdBy(CASEWORKER_ID.toString())
            .name("Hearing admin")
            .build();

        DocumentQueryMessage expected = DocumentQueryMessage.builder()
            .messageType("Caseworker response")
            .id(QUERY_ID)
            .name("Caseworker")
            .createdOn("16-05-2025 13:00")
            .build();

        DocumentQueryMessage actual = DocumentQueryMessage.from(queryResponse, true);
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapFromFollowUpQuery() {
        CaseMessage queryFollowUp = CaseMessage.builder()
            .id(QUERY_ID)
            .parentId(PARENT_QUERY_ID)
            .isHearingRelated(YES)
            .hearingDate(HEARING_DATE)
            .createdOn(DATE_QUERY_RAISED.plusDays(2))
            .createdBy(SOLICITOR_ID.toString())
            .name("Solicitor")
            .build();

        DocumentQueryMessage expected = DocumentQueryMessage.builder()
            .messageType("Follow up")
            .id(QUERY_ID)
            .name("Solicitor")
            .createdOn("17-05-2025 13:00")
            .build();

        DocumentQueryMessage actual = DocumentQueryMessage.from(queryFollowUp, false);

        assertEquals(expected, actual);
    }
}
