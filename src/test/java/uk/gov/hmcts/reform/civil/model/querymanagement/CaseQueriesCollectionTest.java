package uk.gov.hmcts.reform.civil.model.querymanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class CaseQueriesCollectionTest {

    @Test
    void shouldReturnLatestCaseMessage() {
        LocalDateTime now = LocalDateTime.of(2025, 3, 1, 7, 0, 0);
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
}
