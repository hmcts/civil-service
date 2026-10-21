package uk.gov.hmcts.reform.civil.notification.handlers.staylifted;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class StayLiftedNotifier extends Notifier {

    public StayLiftedNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                            StayLiftedAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.StayLiftedNotifyParties.toString();
    }
}
