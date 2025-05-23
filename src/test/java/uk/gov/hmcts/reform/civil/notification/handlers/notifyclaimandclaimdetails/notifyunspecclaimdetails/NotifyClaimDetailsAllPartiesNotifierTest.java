package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.UnspecNotifyClaimDetailsNotifier;

class NotifyClaimDetailsAllPartiesNotifierTest extends NotifierTestBase {

    @InjectMocks
    private NotifyClaimDetailsAllPartiesNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(UnspecNotifyClaimDetailsNotifier.toString());
    }
}
