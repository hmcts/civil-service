package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondentonenotifyotherstrialready;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class RespondentOneNotifyOthersTrialReadyNotifierTest extends NotifierTestBase {

    @InjectMocks
    private RespondentOneNotifyOthersTrialReadyNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo("Respondent1NotifyOthersTrialReady");
    }
}
