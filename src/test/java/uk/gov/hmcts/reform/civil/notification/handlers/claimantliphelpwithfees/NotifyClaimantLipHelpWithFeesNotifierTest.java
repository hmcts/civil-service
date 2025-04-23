package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantLipHelpWithFeesNotifier;

@ExtendWith(MockitoExtension.class)
class NotifyClaimantLipHelpWithFeesNotifierTest extends NotifierTestBase {

    @InjectMocks
    private NotifyClaimantLipHelpWithFeesNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
                .isEqualTo(ClaimantLipHelpWithFeesNotifier.toString());
    }
}
