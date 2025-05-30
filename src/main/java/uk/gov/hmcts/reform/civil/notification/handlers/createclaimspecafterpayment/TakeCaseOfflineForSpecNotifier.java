package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.CreateClaimSpecAfterPaymentContinuingOfflineNotifier;

@Component
public class TakeCaseOfflineForSpecNotifier extends Notifier {

    public TakeCaseOfflineForSpecNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            TakeCaseOfflineForSpecPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return CreateClaimSpecAfterPaymentContinuingOfflineNotifier.toString();
    }
}
