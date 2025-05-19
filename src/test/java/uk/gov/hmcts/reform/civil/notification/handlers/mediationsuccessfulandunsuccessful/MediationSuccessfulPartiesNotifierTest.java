package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

class MediationSuccessfulPartiesNotifierTest extends NotifierTestBase {

    @InjectMocks
    private MediationSuccessfulPartiesNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(MediationSuccessfulNotifyParties.toString());
    }
}
