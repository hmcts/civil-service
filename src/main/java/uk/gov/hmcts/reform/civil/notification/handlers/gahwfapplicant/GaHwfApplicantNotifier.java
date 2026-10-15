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
 * <p>{@link #getTaskId()} returns the BPMN activity id that
 * {@code notify_ga_applicant_hwf_outcome.bpmn} will carry once it raises {@code NOTIFY_EVENT}. The
 * BPMN is not switched in this change because {@code NOTIFY_EVENT} does not yet exist for the
 * GENERALAPPLICATION case type in civil-ccd-definition, so CCD would refuse the event trigger.</p>
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
