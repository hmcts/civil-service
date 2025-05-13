package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.UnspecNotifyClaimDetailsNotifier;

@Component
public class NotifyClaimDetailsAllPartiesNotifier extends Notifier {

    public NotifyClaimDetailsAllPartiesNotifier(NotificationService notificationService,
                                                CaseTaskTrackingService caseTaskTrackingService,
                                                NotifyClaimDetailsAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return UnspecNotifyClaimDetailsNotifier.toString();
    }

}
