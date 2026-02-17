package uk.gov.hmcts.reform.civil.notification.handlers.trialreadynotification;

import static org.assertj.core.api.Assertions.assertThat;

import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class TrialReadyNotificationNotifierTest extends NotifierTestBase {

    @InjectMocks
    private TrialReadyNotificationNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("TrialReadyNotificationNotifier");
    }
}
