package uk.gov.hmcts.reform.civil.notification.handlers.ga.gaapplicationcreated;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class GaApplicationCreatedNotifier extends Notifier<GeneralApplicationCaseData> {

    public static final String TASK_ID = "NotifyGaApplicationCreated";

    public GaApplicationCreatedNotifier(NotificationService notificationService,
                                        CaseTaskTrackingService caseTaskTrackingService,
                                        GaApplicationCreatedAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return TASK_ID;
    }
}
