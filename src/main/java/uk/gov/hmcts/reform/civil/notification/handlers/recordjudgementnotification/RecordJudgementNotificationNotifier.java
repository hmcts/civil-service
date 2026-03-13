package uk.gov.hmcts.reform.civil.notification.handlers.recordjudgementnotification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class RecordJudgementNotificationNotifier extends Notifier {

    public RecordJudgementNotificationNotifier(NotificationService notificationService,
                                               CaseTaskTrackingService caseTaskTrackingService,
                                               RecordJudgementNotificationAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.RecordJudgementNotificationNotifier.toString();
    }

}
