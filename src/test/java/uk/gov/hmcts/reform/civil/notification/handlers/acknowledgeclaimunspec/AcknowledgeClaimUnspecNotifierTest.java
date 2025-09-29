package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AcknowledgeClaimUnspecNotifyParties;

class AcknowledgeClaimUnspecNotifierTest extends NotifierTestBase {

    @InjectMocks
    private AcknowledgeClaimUnspecNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(AcknowledgeClaimUnspecNotifyParties.toString());
    }
}
