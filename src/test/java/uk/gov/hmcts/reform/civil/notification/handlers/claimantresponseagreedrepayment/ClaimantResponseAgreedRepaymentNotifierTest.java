package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedrepayment;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimantResponseAgreedRepaymentNotifierTest extends NotifierTestBase {

    @InjectMocks
    private ClaimantResponseAgreedRepaymentNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo("ClaimantResponseAgreedRepaymentNotify");
    }
}
