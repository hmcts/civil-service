package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;

class CourtOfficerOrderNotifierTest extends NotifierTestBase {

    @InjectMocks
    private CourtOfficerOrderNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(GenerateOrderNotifyPartiesCourtOfficerOrder.toString());
    }

}
