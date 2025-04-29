package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;

@Component
public class ClaimantLipHelpWithFeesNotifier extends Notifier {

    public ClaimantLipHelpWithFeesNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            ClaimantLipHelpWithFeesPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.ClaimantLipHelpWithFeesNotifier.toString();
    }
}