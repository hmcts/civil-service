package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class CreateClaimAfterPaymentContinuingOnlineNotifier extends Notifier {

    public CreateClaimAfterPaymentContinuingOnlineNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            CreateClaimAfterPaymentContinuingOnlineEmailGenerator emailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, emailGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.CreateClaimAfterPaymentContinuingOnlineNotifier.toString();
    }
}
