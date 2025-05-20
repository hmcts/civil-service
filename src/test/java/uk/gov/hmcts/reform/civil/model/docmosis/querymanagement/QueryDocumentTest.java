package uk.gov.hmcts.reform.civil.model.docmosis.querymanagement;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class QueryDocumentTest {

    private static LocalDate HEARING_DATE = LocalDate.of(2025, 3, 15);
    private static OffsetDateTime DATE_QUERY_RAISED = OffsetDateTime.of(LocalDateTime.of(2025, 1, 15, 12, 0, 0), ZoneOffset.UTC);
    private static String CASE_ID = "1111222233334444";
    private static UUID SOLICITOR_ID = UUID.randomUUID();
    private static UUID CASEWORKER_ID = UUID.randomUUID();
    private static String QUERY_ID = "query-id";
    private static String PARENT_QUERY_ID = "parent-id";

    private static CaseMessage INITIAL_QUERY = CaseMessage.builder()
        .id(PARENT_QUERY_ID)
        .name("Solicitor")
        .isHearingRelated(YES)
        .hearingDate(HEARING_DATE)
        .subject("initial query")
        .createdOn(DATE_QUERY_RAISED)
        .createdBy(SOLICITOR_ID.toString())
        .build();

    private static CaseMessage QUERY_RESPONSE =
        CaseMessage.builder()
            .id(QUERY_ID)
            .name("Caseworker")
            .subject("A response")
            .parentId(PARENT_QUERY_ID)
            .isHearingRelated(YES)
            .hearingDate(HEARING_DATE)
            .createdOn(DATE_QUERY_RAISED.plusDays(1))
            .createdBy(CASEWORKER_ID.toString())
            .build();

    private static CaseMessage QUERY_FOLLOWUP =
        CaseMessage.builder()
            .id(QUERY_ID)
            .name("Solicitor")
            .subject("A follow up")
            .parentId(PARENT_QUERY_ID)
            .isHearingRelated(YES)
            .hearingDate(HEARING_DATE)
            .createdOn(DATE_QUERY_RAISED.plusDays(2))
            .createdBy(SOLICITOR_ID.toString())
            .build();

    @Test
    void shouldReturnQueryDocument() {

        List<Element<CaseMessage>> messageThread = List.of(
            element(INITIAL_QUERY),
            element(QUERY_RESPONSE),
            element(QUERY_FOLLOWUP)
        );

        QueryDocument result = QueryDocument.from(CASE_ID, messageThread);

        assertEquals(QueryDocument.builder()
                         .referenceNumber(CASE_ID)
                         .messages(List.of(
                             DocumentQueryMessage.builder()
                                 .id(INITIAL_QUERY.getId())
                                 .messageType("Query")
                                 .name(INITIAL_QUERY.getName())
                                 .subject(INITIAL_QUERY.getSubject())
                                 .hearingDate("15-03-2025")
                                 .createdOn("15-01-2025 12:00")
                                 .isHearingRelated(INITIAL_QUERY.getIsHearingRelated())
                                 .attachments(INITIAL_QUERY.getAttachments())
                                 .build(),
                             DocumentQueryMessage.builder()
                                 .id(QUERY_RESPONSE.getId())
                                 .messageType("Caseworker response")
                                 .name("Caseworker")
                                 .createdOn("16-01-2025 12:00")
                                 .attachments(QUERY_RESPONSE.getAttachments())
                                 .build(),
                             DocumentQueryMessage.builder()
                                 .id(QUERY_FOLLOWUP.getId())
                                 .messageType("Follow up")
                                 .name(QUERY_FOLLOWUP.getName())
                                 .createdOn("17-01-2025 12:00")
                                 .attachments(QUERY_FOLLOWUP.getAttachments())
                                 .build()
                         ))
                         .build(), result);

    }

}
