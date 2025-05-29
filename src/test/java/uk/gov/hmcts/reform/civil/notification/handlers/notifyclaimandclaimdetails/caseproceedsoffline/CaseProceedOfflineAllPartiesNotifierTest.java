package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.caseproceedsoffline;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimProceedsOfflineUnspecNotifyApplicantSolicitor;

class CaseProceedOfflineAllPartiesNotifierTest extends NotifierTestBase {

    @InjectMocks
    private CaseProceedOfflineAllPartiesNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(ClaimProceedsOfflineUnspecNotifyApplicantSolicitor.toString());
    }
}
