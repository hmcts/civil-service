package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ChangeOfRepresentationNotifyParties;

@Component
public class ChangeOfRepresentationNotifier extends Notifier {

    public ChangeOfRepresentationNotifier(NotificationService notificationService,
                                          CaseTaskTrackingService caseTaskTrackingService,
                                          AllNoCPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ChangeOfRepresentationNotifyParties.toString();
    }
}
