package uk.gov.hmcts.reform.civil.notification.handlers.defendantsignsettlementagreement;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantSignSettlementNotify;

public class DefendantSignSettlementNotifierTest extends NotifierTestBase {

    @InjectMocks
    private DefendantSignSettlementNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(DefendantSignSettlementNotify.toString());
    }
}
