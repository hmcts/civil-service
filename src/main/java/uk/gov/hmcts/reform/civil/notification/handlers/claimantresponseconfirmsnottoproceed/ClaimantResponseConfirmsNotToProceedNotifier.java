package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantResponseConfirmsNotToProceedNotify;

@Component
public class ClaimantResponseConfirmsNotToProceedNotifier extends Notifier {

    public ClaimantResponseConfirmsNotToProceedNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                        ClaimantResponseConfirmsNotToProceedAllLegalRepsEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ClaimantResponseConfirmsNotToProceedNotify.toString();
    }

}
