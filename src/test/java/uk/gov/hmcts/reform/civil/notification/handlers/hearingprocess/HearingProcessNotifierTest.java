package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class HearingProcessNotifierTest extends NotifierTestBase {

    @InjectMocks
    private HearingProcessNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("HearingProcessNotifier");
    }
}
