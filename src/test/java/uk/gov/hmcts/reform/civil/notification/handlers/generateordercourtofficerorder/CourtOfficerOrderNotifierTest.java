package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;

@ExtendWith(MockitoExtension.class)
class CourtOfficerOrderNotifierTest extends NotifierTestBase {

    private static final String TASK_ID = "GenerateOrderNotifyPartiesCourtOfficerOrder";

    @InjectMocks
    private CourtOfficerOrderNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(GenerateOrderNotifyPartiesCourtOfficerOrder.toString());
    }
}
