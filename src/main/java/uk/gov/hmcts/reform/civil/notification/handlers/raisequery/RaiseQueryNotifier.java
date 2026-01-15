package uk.gov.hmcts.reform.civil.notification.handlers.raisequery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class RaiseQueryNotifier extends Notifier {

    public RaiseQueryNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        RaiseQueryAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.RaiseQueryNotifier.toString();
    }
}
