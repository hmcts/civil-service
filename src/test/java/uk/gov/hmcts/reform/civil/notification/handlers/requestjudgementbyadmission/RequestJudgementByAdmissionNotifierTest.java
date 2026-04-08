package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

class RequestJudgementByAdmissionNotifierTest extends NotifierTestBase {

    @InjectMocks
    private RequestJudgementByAdmissionNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("RequestJudgementByAdmissionNotifyParties");
    }
}
