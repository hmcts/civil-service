package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

class DjNonDivergentNotifierTest extends NotifierTestBase {

    @InjectMocks
    private DjNonDivergentNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("DJ_NON_DIVERGENT_NOTIFIER");
    }
}
