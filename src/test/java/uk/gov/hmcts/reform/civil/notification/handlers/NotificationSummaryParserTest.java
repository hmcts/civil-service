package uk.gov.hmcts.reform.civil.notification.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The split between the two halves of a notifier summary is what the Record notification details
 * history event is built from, on both Civil and General Applications, so the edges are pinned here
 * rather than in either handler.
 */
class NotificationSummaryParserTest {

    @Test
    void shouldReturnTheRecipientsWithoutThePrefix() {
        assertThat(NotificationSummaryParser.attempted("Attempted: a@b.com : ref : tmpl || Errors: "))
            .isEqualTo("a@b.com : ref : tmpl");
    }

    @Test
    void shouldReturnNullWhenThereAreNoErrors() {
        assertThat(NotificationSummaryParser.errors("Attempted: a@b.com : ref : tmpl || Errors: "))
            .isNull();
    }

    @Test
    void shouldReturnTheErrorsWhenThereAreSome() {
        assertThat(NotificationSummaryParser.errors("Attempted: a@b.com || Errors: it failed"))
            .isEqualTo("it failed");
    }

    @Test
    void shouldTolerateASummaryWithNoDelimiter() {
        assertThat(NotificationSummaryParser.attempted("Attempted: a@b.com")).isEqualTo("a@b.com");
        assertThat(NotificationSummaryParser.errors("Attempted: a@b.com")).isNull();
    }

    @Test
    void shouldTolerateASummaryWithNoPrefix() {
        assertThat(NotificationSummaryParser.attempted("a@b.com")).isEqualTo("a@b.com");
    }

    @Test
    void shouldTolerateNull() {
        assertThat(NotificationSummaryParser.attempted(null)).isNull();
        assertThat(NotificationSummaryParser.errors(null)).isNull();
    }
}
