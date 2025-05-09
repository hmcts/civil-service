package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseSpecOneRespRespondedNotifyParties;

class SpecOneDefRespDefResponseNotifierTest extends NotifierTestBase {

    @InjectMocks
    private SpecOneDefRespDefResponseNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(DefendantResponseSpecOneRespRespondedNotifyParties.toString());
    }
}
