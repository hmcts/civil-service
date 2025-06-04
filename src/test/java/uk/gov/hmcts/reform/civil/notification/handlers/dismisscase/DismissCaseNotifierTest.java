package uk.gov.hmcts.reform.civil.notification.handlers.dismisscase;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class DismissCaseNotifierTest extends NotifierTestBase {

    @InjectMocks
    private DismissCaseNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("DismissCaseNotify");
    }
}
