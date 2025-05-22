package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimanthwfoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NotifyClaimantHwFOutcomeNotifierTest {

    private NotifyClaimantHwFOutcomeNotifier notifier;

    @BeforeEach
    void setUp() {
        NotificationService notificationService = mock(NotificationService.class);
        CaseTaskTrackingService caseTaskTrackingService = mock(CaseTaskTrackingService.class);
        NotifyClaimantHwFOutcomeAllLegalRepsEmailGenerator emailGenerator = mock(
            NotifyClaimantHwFOutcomeAllLegalRepsEmailGenerator.class);
        notifier = new NotifyClaimantHwFOutcomeNotifier(notificationService, caseTaskTrackingService, emailGenerator);
    }

    @Test
    void shouldExtendNotifier() {
        assertThat(notifier).isInstanceOf(Notifier.class);
    }

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo("HwFOutcomeNotify");
    }
}
