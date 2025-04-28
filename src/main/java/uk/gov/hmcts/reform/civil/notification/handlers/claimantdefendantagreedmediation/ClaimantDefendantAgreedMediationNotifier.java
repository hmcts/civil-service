package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantDefendantAgreedMediationNotify;

@Component
public class ClaimantDefendantAgreedMediationNotifier extends Notifier {


    public ClaimantDefendantAgreedMediationNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService, ClaimantDefendantAgreedMediationPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ClaimantDefendantAgreedMediationNotify.toString();
    }
}
