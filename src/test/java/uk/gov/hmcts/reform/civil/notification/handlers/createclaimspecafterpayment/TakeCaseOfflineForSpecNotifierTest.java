package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.CreateClaimSpecAfterPaymentContinuingOfflineNotifier;

@ExtendWith(MockitoExtension.class)
class TakeCaseOfflineForSpecNotifierTest {

    @InjectMocks
    private TakeCaseOfflineForSpecNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
                .isEqualTo(CreateClaimSpecAfterPaymentContinuingOfflineNotifier.toString());
    }
}
