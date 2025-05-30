package uk.gov.hmcts.reform.civil.notification.handlers.createlipclaim;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantLipHelpWithFeesNotifier;

@Component
public class ApplicantClaimSubmittedNotifier extends Notifier {

    public ApplicantClaimSubmittedNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            ApplicantClaimSubmittedAllPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return ClaimantLipHelpWithFeesNotifier.toString();
    }
}
