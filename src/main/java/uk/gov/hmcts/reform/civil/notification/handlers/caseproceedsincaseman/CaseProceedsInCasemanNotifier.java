package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.CaseProceedsInCasemanNotify;

@Component
public class CaseProceedsInCasemanNotifier extends Notifier {

    public CaseProceedsInCasemanNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService, CaseProceedsInCasemanPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CaseProceedsInCasemanNotify.toString();
    }

}
