package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsepartadmitpayimmediately;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimantResponsePartAdmitPayImmediatelyNotifierTest extends NotifierTestBase {

    @InjectMocks
    private ClaimantResponsePartAdmitPayImmediatelyNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo("ClaimantResponsePartAdmitPayImmediatelyNotifier");
    }
}
