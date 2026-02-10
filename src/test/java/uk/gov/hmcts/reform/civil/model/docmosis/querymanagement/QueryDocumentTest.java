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

    private static final CaseMessage INITIAL_QUERY = createCaseMessage(
        PARENT_QUERY_ID,
        "Solicitor",
        "initial query",
        null,
        SOLICITOR_ID,
        DATE_QUERY_RAISED
    );

    private static final CaseMessage QUERY_RESPONSE = createCaseMessage(
        QUERY_ID,
        "Caseworker",
        "A response",
        PARENT_QUERY_ID,
        CASEWORKER_ID,
        DATE_QUERY_RAISED.plusDays(1)
    );

    private static final CaseMessage QUERY_FOLLOWUP = createCaseMessage(
        QUERY_ID,
        "Solicitor",
        "A follow up",
        PARENT_QUERY_ID,
        SOLICITOR_ID,
        DATE_QUERY_RAISED.plusDays(2)
    );

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

    private static CaseMessage createCaseMessage(String id,
                                                 String name,
                                                 String subject,
                                                 String parentId,
                                                 UUID createdBy,
                                                 OffsetDateTime createdOn) {
        CaseMessage caseMessage = new CaseMessage();
        caseMessage.setId(id);
        caseMessage.setName(name);
        caseMessage.setSubject(subject);
        caseMessage.setParentId(parentId);
        caseMessage.setIsHearingRelated(YES);
        caseMessage.setHearingDate(HEARING_DATE);
        caseMessage.setCreatedOn(createdOn);
        caseMessage.setCreatedBy(createdBy.toString());
        return caseMessage;
    }

}
