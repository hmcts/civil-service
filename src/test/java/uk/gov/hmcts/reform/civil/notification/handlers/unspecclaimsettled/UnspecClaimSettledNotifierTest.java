package uk.gov.hmcts.reform.civil.notification.handlers.unspecclaimsettled;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

class UnspecClaimSettledNotifierTest extends NotifierTestBase {

    @InjectMocks
    private UnspecClaimSettledNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo("UnspecClaimSettledNotifier");
    }
}
