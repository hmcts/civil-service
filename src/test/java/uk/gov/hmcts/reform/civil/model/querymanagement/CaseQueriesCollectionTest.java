package uk.gov.hmcts.reform.civil.model.querymanagement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        LocalDateTime now = LocalDateTime.now();
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
        LocalDateTime now = LocalDateTime.now();
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

}
