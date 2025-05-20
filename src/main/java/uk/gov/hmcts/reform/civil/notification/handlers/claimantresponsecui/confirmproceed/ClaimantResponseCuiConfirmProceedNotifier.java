package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantConfirmProceedNotifyParties;

@Component
public class ClaimantResponseCuiConfirmProceedNotifier extends Notifier {

    public ClaimantResponseCuiConfirmProceedNotifier(NotificationService notificationService,
                                                     CaseTaskTrackingService caseTaskTrackingService,
                                                     ClaimantRespConfirmProceedClaimantEmailGenerator claimantEmailGenerator) {
        super(notificationService, caseTaskTrackingService, claimantEmailGenerator);
    }

    @Override
    protected String getTaskId() {
        return ClaimantConfirmProceedNotifyParties.name();
    }
}
