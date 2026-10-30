package uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import org.junit.jupiter.api.Test;

class GaHearingScheduledNotifierTest {

    @Test
    void shouldReturnTheActivityIdTheMigratedBpmnRaises() {
        GaHearingScheduledNotifier notifier = new GaHearingScheduledNotifier(
            mock(NotificationService.class), mock(CaseTaskTrackingService.class),
            mock(GaHearingScheduledAllPartiesEmailGenerator.class));

        // a mismatch with ga_hearing_scheduled_access.bpmn compiles cleanly and only shows up as
        // "no notifier found" on a live case, so it is asserted rather than assumed
        assertThat(notifier.getTaskId()).isEqualTo("NotifyGaHearingScheduled");
    }
}
