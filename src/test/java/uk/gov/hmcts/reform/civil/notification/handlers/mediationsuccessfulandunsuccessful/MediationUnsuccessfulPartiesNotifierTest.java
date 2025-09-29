package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationUnsuccessfulNotifyParties;

class MediationUnsuccessfulPartiesNotifierTest extends NotifierTestBase {

    @InjectMocks
    private MediationUnsuccessfulPartiesNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(MediationUnsuccessfulNotifyParties.toString());
    }
}
