package uk.gov.hmcts.reform.civil.notification.handlers.extendresponsedeadline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class ExtendResponseDeadlineNotifier extends Notifier {

    public ExtendResponseDeadlineNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        ExtendResponseDeadlineAllPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.ExtendResponseDeadlineNotifier.toString();
    }
}
