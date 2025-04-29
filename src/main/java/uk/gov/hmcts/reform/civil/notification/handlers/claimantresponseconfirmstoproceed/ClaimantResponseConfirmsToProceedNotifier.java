package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantConfirmsToProceedNotify;

@Component
public class ClaimantResponseConfirmsToProceedNotifier extends Notifier {

    public ClaimantResponseConfirmsToProceedNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                     ClaimantResponseConfirmsToProceedPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ClaimantConfirmsToProceedNotify.toString();
    }

}
