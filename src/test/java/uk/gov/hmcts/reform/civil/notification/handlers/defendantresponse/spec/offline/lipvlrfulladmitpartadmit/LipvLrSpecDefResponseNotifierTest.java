package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.lipvlrfulladmitpartadmit;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseSpecLipvLRFullOrPartAdmit;

class LipvLrSpecDefResponseNotifierTest extends NotifierTestBase {

    @InjectMocks
    private LipvLrSpecDefResponseNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(DefendantResponseSpecLipvLRFullOrPartAdmit.toString());
    }
}
