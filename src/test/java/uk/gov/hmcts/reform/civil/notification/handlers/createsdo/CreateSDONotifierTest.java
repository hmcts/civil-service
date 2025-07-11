package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateSDONotifierTest extends NotifierTestBase {

    @InjectMocks
    private CreateSDONotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo("CreateSDONotify");
    }
}
