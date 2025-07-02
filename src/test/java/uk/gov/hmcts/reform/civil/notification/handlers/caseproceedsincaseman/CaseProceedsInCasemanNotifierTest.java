package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseProceedsInCasemanNotifierTest extends NotifierTestBase {

    @InjectMocks
    private CaseProceedsInCasemanNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("CaseProceedsInCasemanNotify");
    }
}
