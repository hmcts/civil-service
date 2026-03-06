package uk.gov.hmcts.reform.civil.notification.handlers.initiatecoscapplication;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class InitiateCoscApplicationNotifier extends Notifier {

    public InitiateCoscApplicationNotifier(NotificationService notificationService,
                                           CaseTaskTrackingService caseTaskTrackingService,
                                           InitiateCoscApplicationAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.InitiateCoscApplicationNotifier.toString();
    }
}
