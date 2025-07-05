package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecui;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseCUINotify;

@Component
public class DefendantResponseCUINotifier extends Notifier {

    protected DefendantResponseCUINotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                           DefendantResponseCUIAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return DefendantResponseCUINotify.toString();
    }
}
