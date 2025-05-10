package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlip;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantLipRepresentedWithNoCNotifier;

@Component
public class ClaimantLipRepresentedNotifier extends Notifier {

    public ClaimantLipRepresentedNotifier(NotificationService notificationService,
                                          CaseTaskTrackingService caseTaskTrackingService,
                                          ClaimantLipRepresentedAllPartiesEmailDTOGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ClaimantLipRepresentedWithNoCNotifier.toString();
    }
}
