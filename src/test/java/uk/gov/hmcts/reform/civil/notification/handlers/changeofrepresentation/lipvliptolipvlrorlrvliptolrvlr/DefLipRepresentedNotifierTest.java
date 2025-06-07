package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantLipRepresentedWithNoCNotifier;

class DefLipRepresentedNotifierTest extends NotifierTestBase {

    @InjectMocks
    private DefLipRepresentedNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo(DefendantLipRepresentedWithNoCNotifier.toString());
    }
}
