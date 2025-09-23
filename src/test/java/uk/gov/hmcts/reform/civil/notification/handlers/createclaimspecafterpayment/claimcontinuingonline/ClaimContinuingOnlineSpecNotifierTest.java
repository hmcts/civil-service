package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ContinuingClaimOnlineSpecClaimNotifier;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecNotifierTest {

    @InjectMocks
    private ClaimContinuingOnlineSpecNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
                .isEqualTo(ContinuingClaimOnlineSpecClaimNotifier.toString());
    }
}
