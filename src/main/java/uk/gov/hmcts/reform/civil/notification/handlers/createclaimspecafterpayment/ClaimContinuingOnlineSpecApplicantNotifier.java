package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ContinuingOnlineSpecClaimApplicantNotifier;

@Component
public class ClaimContinuingOnlineSpecApplicantNotifier extends Notifier {

    public ClaimContinuingOnlineSpecApplicantNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            ClaimContinuingOnlineSpecApplicantEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return ContinuingOnlineSpecClaimApplicantNotifier.toString();
    }
}
