package uk.gov.hmcts.reform.civil.notification.handlers.settleclaimpaidinfullnotification;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

class SettleClaimPaidInFullNotificationNotifierTest extends NotifierTestBase {

    @InjectMocks
    private SettleClaimPaidInFullNotificationNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo("SettleClaimPaidInFullNotificationNotifier");
    }
}
