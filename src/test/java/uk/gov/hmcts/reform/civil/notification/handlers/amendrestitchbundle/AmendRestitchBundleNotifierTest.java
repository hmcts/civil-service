package uk.gov.hmcts.reform.civil.notification.handlers.amendrestitchbundle;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class AmendRestitchBundleNotifierTest extends NotifierTestBase {

    @InjectMocks
    private AmendRestitchBundleNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("AmendRestitchBundleNotify");
    }
}
