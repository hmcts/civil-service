package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceedlip;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantResponseConfirmsNotToProceedLipNotify;

@Component
public class ClaimantResponseConfirmsNotToProceedLipNotifier extends Notifier {

    protected ClaimantResponseConfirmsNotToProceedLipNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                              ClaimantConfirmsNotToProceedLipPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ClaimantResponseConfirmsNotToProceedLipNotify.toString();
    }
}
