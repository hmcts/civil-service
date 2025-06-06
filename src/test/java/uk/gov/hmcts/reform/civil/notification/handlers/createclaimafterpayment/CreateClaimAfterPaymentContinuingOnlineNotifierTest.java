package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.CreateClaimAfterPaymentContinuingOnlineNotifier;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentContinuingOnlineNotifierTest extends NotifierTestBase {

    @InjectMocks
    private CreateClaimAfterPaymentContinuingOnlineNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
                .isEqualTo(CreateClaimAfterPaymentContinuingOnlineNotifier.toString());
    }
}