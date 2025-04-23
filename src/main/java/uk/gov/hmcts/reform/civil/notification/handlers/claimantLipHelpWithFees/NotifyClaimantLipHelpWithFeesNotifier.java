package uk.gov.hmcts.reform.civil.notification.handlers.claimantLipHelpWithFees;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantLipHelpWithFeesNotifier;

@Component
public class NotifyClaimantLipHelpWithFeesNotifier extends Notifier {

    public NotifyClaimantLipHelpWithFeesNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            NotifyClaimantLipHelpWithFeesPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return ClaimantLipHelpWithFeesNotifier.toString();
    }
}
