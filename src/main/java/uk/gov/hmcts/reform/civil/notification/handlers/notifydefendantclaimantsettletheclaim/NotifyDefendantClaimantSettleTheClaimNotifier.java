package uk.gov.hmcts.reform.civil.notification.handlers.notifydefendantclaimantsettletheclaim;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.NotifyDefendantClaimantSettleTheClaimNotify;

@Component
public class NotifyDefendantClaimantSettleTheClaimNotifier extends Notifier<CaseData> {

    protected NotifyDefendantClaimantSettleTheClaimNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        NotifyDefendantClaimantSettleTheClaimAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return NotifyDefendantClaimantSettleTheClaimNotify.toString();
    }
}
