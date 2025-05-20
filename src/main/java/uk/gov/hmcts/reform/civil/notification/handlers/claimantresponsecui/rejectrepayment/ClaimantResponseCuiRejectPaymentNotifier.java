package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.rejectrepayment;

import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

public class ClaimantResponseCuiRejectPaymentNotifier extends Notifier {
    public ClaimantResponseCuiRejectPaymentNotifier(NotificationService notificationService,
                                                    CaseTaskTrackingService caseTaskTrackingService,
                                                    PartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return null;
    }
}
