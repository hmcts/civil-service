package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.CreateClaimAfterPaymentContinuingOfflineNotifier;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentOfflineNotifierTest extends NotifierTestBase {

    @InjectMocks
    private CreateClaimAfterPaymentOfflineNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
                .isEqualTo(CreateClaimAfterPaymentContinuingOfflineNotifier.toString());
    }
}