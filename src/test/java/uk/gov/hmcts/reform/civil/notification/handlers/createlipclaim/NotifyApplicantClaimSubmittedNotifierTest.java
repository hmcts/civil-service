package uk.gov.hmcts.reform.civil.notification.handlers.createlipclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ApplicantClaimSubmittedNotifier;

@ExtendWith(MockitoExtension.class)
public class NotifyApplicantClaimSubmittedNotifierTest extends NotifierTestBase {

    @InjectMocks
    private NotifyApplicantClaimSubmittedNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
                .isEqualTo(ApplicantClaimSubmittedNotifier.toString());
    }
}
