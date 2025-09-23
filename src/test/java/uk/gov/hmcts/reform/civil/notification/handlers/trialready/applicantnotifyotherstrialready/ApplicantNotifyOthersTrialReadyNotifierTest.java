package uk.gov.hmcts.reform.civil.notification.handlers.trialready.applicantnotifyotherstrialready;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicantNotifyOthersTrialReadyNotifierTest extends NotifierTestBase {

    @InjectMocks
    private ApplicantNotifyOthersTrialReadyNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo("ApplicantNotifyOthersTrialReady");
    }
}
