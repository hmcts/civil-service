package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.CreateSDONotify;

@Component
public class CreateSDONotifier extends Notifier {

    public CreateSDONotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService, CreateSDOPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    public String getTaskId() {
        return CreateSDONotify.toString();
    }

}
