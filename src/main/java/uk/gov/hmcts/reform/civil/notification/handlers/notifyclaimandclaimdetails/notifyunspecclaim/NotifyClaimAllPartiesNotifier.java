package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.UnspecNotifyClaimNotifier;

@Component
public class NotifyClaimAllPartiesNotifier extends Notifier<CaseData> {

    public NotifyClaimAllPartiesNotifier(NotificationService notificationService,
                                                CaseTaskTrackingService caseTaskTrackingService,
                                                NotifyClaimAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return UnspecNotifyClaimNotifier.toString();
    }
}
