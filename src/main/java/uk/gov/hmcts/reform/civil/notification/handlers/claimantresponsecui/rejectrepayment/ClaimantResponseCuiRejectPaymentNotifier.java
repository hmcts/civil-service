package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.rejectrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.RejectRepaymentPlanNotifyParties;

@Component
public class ClaimantResponseCuiRejectPaymentNotifier extends Notifier {

    public ClaimantResponseCuiRejectPaymentNotifier(NotificationService notificationService,
                                                    CaseTaskTrackingService caseTaskTrackingService,
                                                    ClaimantResponseCuiRejectPayAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return RejectRepaymentPlanNotifyParties.name();
    }
}
