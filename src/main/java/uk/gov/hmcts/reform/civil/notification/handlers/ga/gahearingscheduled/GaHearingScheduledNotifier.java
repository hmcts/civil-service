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
 * {@code NOTIFY_EVENT}. Both definitions that raised those events now do so:
 * {@code ga_hearing_scheduled_access.bpmn} for the English notice and
 * {@code upload_translated_ga_hearing_scheduled_access.bpmn} for the Welsh translated one, which
 * is why the two share this activity id. Migrating only the first would have left the Welsh path
 * on the old handlers, with different error handling and nothing to stop someone deleting them.
 * {@code GaHearingScheduledTest} and {@code UploadTranslatedHearingNoticeGaTest} both drive their
 * deployed BPMN with {@link #TASK_ID}, so neither can drift from the id in the XML.</p>
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
