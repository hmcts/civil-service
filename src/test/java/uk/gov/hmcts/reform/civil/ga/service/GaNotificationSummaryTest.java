package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.notification.handlers.NotificationSummaryParser;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class GaNotificationSummaryTest {

    private final NotificationService service = mock(NotificationService.class);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldOmitMissingAddresses(String email) {
        var summary = new GaNotificationSummary(service);
        summary.send(email, "template", Map.of(), "reference");
        assertThat(summary.build()).isEqualTo("Attempted:  || Errors: ");
        verifyNoInteractions(service);
    }

    @Test
    void shouldDescribeFailuresWithoutAMessage() {
        doThrow(new IllegalStateException()).when(service).sendMail("a@example.com", "template", Map.of(), "reference");
        var summary = new GaNotificationSummary(service);
        summary.send("a@example.com", "template", Map.of(), "reference");
        assertThat(NotificationSummaryParser.attempted(summary.build())).isEqualTo("a@example.com : reference : template");
        assertThat(NotificationSummaryParser.errors(summary.build())).contains("a@example.com", "IllegalStateException");
    }
}
