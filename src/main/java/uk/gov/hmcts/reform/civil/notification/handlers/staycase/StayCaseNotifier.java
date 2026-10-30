package uk.gov.hmcts.reform.civil.notification.handlers.staycase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class StayCaseNotifier extends Notifier {

    public StayCaseNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                            StayCaseAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.StayCaseNotifyParties.toString();
    }
}
