package uk.gov.hmcts.reform.civil.notification.handlers.notifylipresetpin;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class NotifyLipResetPinNotifier extends Notifier {

    protected NotifyLipResetPinNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService, PartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.NotifyLipResetPinNotifier.toString();
    }
}
