package uk.gov.hmcts.reform.civil.notification.handlers.coscapplicationafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class CoscApplicationAfterPaymentNotifier extends Notifier {

    public CoscApplicationAfterPaymentNotifier(NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService, CoscApplicationAfterPaymentAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.CoscApplicationAfterPaymentNotifyParties.toString();
    }
}
