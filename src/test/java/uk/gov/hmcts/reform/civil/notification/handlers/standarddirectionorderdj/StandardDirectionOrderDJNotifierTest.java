package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

class StandardDirectionOrderDJNotifierTest extends NotifierTestBase {

    @InjectMocks
    private StandardDirectionOrderDJNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo("STANDARD_DIRECTION_ORDER_DJ_NOTIFY_PARTIES");
    }
}
