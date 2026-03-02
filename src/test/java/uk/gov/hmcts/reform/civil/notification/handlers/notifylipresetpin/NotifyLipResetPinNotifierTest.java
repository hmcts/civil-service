package uk.gov.hmcts.reform.civil.notification.handlers.notifylipresetpin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotifyLipResetPinNotifierTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseTaskTrackingService caseTaskTrackingService;

    @Mock
    private NotifyLipResetPinAllPartiesEmailGenerator allPartiesEmailGenerator;

    @InjectMocks
    private NotifyLipResetPinNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo(CamundaProcessIdentifier.NotifyLipResetPinNotifier.toString());
    }
}
