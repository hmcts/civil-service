package uk.gov.hmcts.reform.civil.notification.handlers.stayupdaterequested;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class StayUpdateRequestedNotifier extends Notifier {

    public StayUpdateRequestedNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                            StayUpdateRequestedAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.StayUpdateRequestedNotifyParties.toString();
    }
}
