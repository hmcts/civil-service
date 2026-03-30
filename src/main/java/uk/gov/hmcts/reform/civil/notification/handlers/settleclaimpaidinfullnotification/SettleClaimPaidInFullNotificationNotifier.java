package uk.gov.hmcts.reform.civil.notification.handlers.settleclaimpaidinfullnotification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class SettleClaimPaidInFullNotificationNotifier extends Notifier {

    public SettleClaimPaidInFullNotificationNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        SettleClaimPaidInFullNotificationAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    protected String getTaskId() {
        return  CamundaProcessIdentifier.SettleClaimPaidInFullNotificationNotifier.toString();
    }
}
