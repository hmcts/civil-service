package uk.gov.hmcts.reform.civil.notification.handlers.judgmentbyadmission;

import static org.assertj.core.api.Assertions.assertThat;

import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class JudgmentByAdmissionNotifierTest extends NotifierTestBase {

    @InjectMocks
    private JudgmentByAdmissionNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("JudgmentByAdmissionNotifier");
    }
}
