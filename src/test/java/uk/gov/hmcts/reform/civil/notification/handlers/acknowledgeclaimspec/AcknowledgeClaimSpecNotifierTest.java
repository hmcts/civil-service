package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AcknowledgeSpecClaimNotifier;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimSpecNotifierTest extends NotifierTestBase {

    @InjectMocks
    private AcknowledgeClaimSpecNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
                .isEqualTo(AcknowledgeSpecClaimNotifier.toString());
    }
}
