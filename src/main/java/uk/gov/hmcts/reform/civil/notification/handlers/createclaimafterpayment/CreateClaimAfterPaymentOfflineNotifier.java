package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class CreateClaimAfterPaymentOfflineNotifier extends Notifier<CaseData> {

    public CreateClaimAfterPaymentOfflineNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            CreateClaimAfterPaymentOfflineAllPartiesEmailGenerator emailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, emailGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.CreateClaimAfterPaymentContinuingOfflineNotifier.toString();
    }
}
