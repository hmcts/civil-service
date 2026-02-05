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

        assertEquals(new QueryDocument()
                         .setReferenceNumber(CASE_ID)
                         .setMessages(List.of(
                             new DocumentQueryMessage()
                                 .setId(INITIAL_QUERY.getId())
                                 .setMessageType("Query")
                                 .setName(INITIAL_QUERY.getName())
                                 .setSubject(INITIAL_QUERY.getSubject())
                                 .setHearingDate("15-03-2025")
                                 .setCreatedOn("15-01-2025 12:00")
                                 .setIsHearingRelated(INITIAL_QUERY.getIsHearingRelated())
                                 .setAttachments(INITIAL_QUERY.getAttachments()),
                             new DocumentQueryMessage()
                                 .setId(QUERY_RESPONSE.getId())
                                 .setMessageType("Caseworker response")
                                 .setName("Caseworker")
                                 .setCreatedOn("16-01-2025 12:00")
                                 .setAttachments(QUERY_RESPONSE.getAttachments()),
                             new DocumentQueryMessage()
                                 .setId(QUERY_FOLLOWUP.getId())
                                 .setMessageType("Follow up")
                                 .setName(QUERY_FOLLOWUP.getName())
                                 .setCreatedOn("17-01-2025 12:00")
                                 .setAttachments(QUERY_FOLLOWUP.getAttachments())
                         )), result);

    }

}
