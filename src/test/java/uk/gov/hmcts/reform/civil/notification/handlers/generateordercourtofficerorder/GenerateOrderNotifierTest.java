package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyParties;

@ExtendWith(MockitoExtension.class)
public class GenerateOrderNotifierTest extends NotifierTestBase {

    @Mock
    private GenerateOrderCOOAllPartiesEmailGenerator emailGenerator;

    @InjectMocks
    private GeneratorOrderNotifier notifier;

    @Test
    void shouldSetTaskInfoOnEmailGenerator() {
        // When the GeneratorOrderNotifier is created, it should set taskInfo on emailGenerator
        notifier = new GeneratorOrderNotifier(notificationService, caseTaskTrackingService, emailGenerator);

        // Verify that setTaskInfo was called once with the correct task ID
        verify(emailGenerator, times(1)).setTaskInfo(GenerateOrderNotifyParties.toString());
    }

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(GenerateOrderNotifyParties.toString());
    }
}
