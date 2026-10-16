package uk.gov.hmcts.reform.civil.notification.handlers.gahwfapplicant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

/**
 * First General Application notifier on the framework, replacing
 * {@code HwfApplicantNotificationHandler} and its bespoke {@code NOTIFY_APPLICANT_LIP_HWF} event.
 *
 * <p>{@link #getTaskId()} returns the activity id of the notify service task in
 * {@code notify_ga_applicant_hwf_outcome.bpmn}, which is how {@code GaNotifierFactory} resolves
 * this notifier. The two are asserted against each other in {@code GaHwfNotifyTest}, because a
 * mismatch compiles cleanly and only shows up as "no notifier found" on a live case.</p>
 */
@Component
public class GaHwfApplicantNotifier extends Notifier<GeneralApplicationCaseData> {

    public static final String TASK_ID = "NotifyGaApplicantHwfOutcome";

    public GaHwfApplicantNotifier(NotificationService notificationService,
                                  CaseTaskTrackingService caseTaskTrackingService,
                                  PartiesEmailGenerator<GeneralApplicationCaseData> partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return TASK_ID;
    }
}
