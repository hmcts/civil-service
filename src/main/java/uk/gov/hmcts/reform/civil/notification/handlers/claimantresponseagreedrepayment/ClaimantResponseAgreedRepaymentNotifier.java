package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantResponseAgreedRepaymentNotify;

@Component
public class ClaimantResponseAgreedRepaymentNotifier extends Notifier {

    protected ClaimantResponseAgreedRepaymentNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                      ClaimantResponseAgreedRepaymentPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ClaimantResponseAgreedRepaymentNotify.toString();
    }
}
