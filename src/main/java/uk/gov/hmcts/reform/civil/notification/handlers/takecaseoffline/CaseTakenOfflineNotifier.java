package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class CaseTakenOfflineNotifier extends Notifier {

    public CaseTakenOfflineNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            CaseTakenOfflineAllPartiesEmailGenerator emailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, emailGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.TakeCaseOfflineNotifier.toString();
    }
}
