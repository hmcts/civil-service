package uk.gov.hmcts.reform.civil.notification.handlers.createlipclaim;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimSubmittedApplicantNotifier;

@Component
public class ApplicantClaimSubmittedNotifier extends Notifier<CaseData> {

    public ApplicantClaimSubmittedNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            ApplicantClaimSubmittedAllPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return ClaimSubmittedApplicantNotifier.toString();
    }
}
