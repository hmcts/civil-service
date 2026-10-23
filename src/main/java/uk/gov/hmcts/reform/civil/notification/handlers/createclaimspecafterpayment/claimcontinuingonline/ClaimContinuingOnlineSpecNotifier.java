package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ContinuingClaimOnlineSpecClaimNotifier;

@Component
public class ClaimContinuingOnlineSpecNotifier extends Notifier<CaseData> {

    public ClaimContinuingOnlineSpecNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            ClaimContinuingOnlineSpecAllPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return ContinuingClaimOnlineSpecClaimNotifier.toString();
    }
}
