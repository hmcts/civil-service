package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecui;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class DefendantResponseCUINotifierTest extends NotifierTestBase {

    @InjectMocks
    private DefendantResponseCUINotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo("DefendantResponseCUINotify");
    }
}
