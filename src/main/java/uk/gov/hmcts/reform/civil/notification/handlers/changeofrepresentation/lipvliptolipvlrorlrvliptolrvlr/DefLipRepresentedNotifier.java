package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantLipRepresentedWithNoCNotifier;

@Component
public class DefLipRepresentedNotifier extends Notifier {

    public DefLipRepresentedNotifier(NotificationService notificationService,
                                          CaseTaskTrackingService caseTaskTrackingService,
                                          DefRepresentedAllPartiesEmailDTOGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return DefendantLipRepresentedWithNoCNotifier.toString();
    }
}
