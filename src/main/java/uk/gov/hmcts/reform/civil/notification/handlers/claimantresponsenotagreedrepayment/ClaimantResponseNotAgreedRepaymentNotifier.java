package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantResponseNotAgreedRepaymentNotify;

@Component
public class ClaimantResponseNotAgreedRepaymentNotifier extends Notifier<CaseData> {

    public ClaimantResponseNotAgreedRepaymentNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                      ClaimantResponseNotAgreedRepaymentPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ClaimantResponseNotAgreedRepaymentNotify.toString();
    }

}
