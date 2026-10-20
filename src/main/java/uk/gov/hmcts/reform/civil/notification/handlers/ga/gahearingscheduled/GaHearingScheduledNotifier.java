package uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

/**
 * Hearing notice for a General Application, the second GA notification on the framework.
 *
 * <p>Replaces {@code NotifyHearingNoticeClaimantHandler} and
 * {@code NotifyHearingNoticeDefendantHandler}, and their two bespoke case events, with one
 * {@code NOTIFY_EVENT} raised by {@code ga_hearing_scheduled_access.bpmn}. The activity id below is
 * the one that BPMN carries, and {@code GaHwfNotifyTest}'s sibling asserts the two agree.</p>
 */
@Component
public class GaHearingScheduledNotifier extends Notifier<GeneralApplicationCaseData> {

    public static final String TASK_ID = "NotifyGaHearingScheduled";

    public GaHearingScheduledNotifier(NotificationService notificationService,
                                      CaseTaskTrackingService caseTaskTrackingService,
                                      GaHearingScheduledAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return TASK_ID;
    }
}
