package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimantResponseNotAgreedRepaymentNotifierTest extends NotifierTestBase {
    @InjectMocks
    private ClaimantResponseNotAgreedRepaymentNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("ClaimantResponseNotAgreedRepaymentNotify");
    }
}
