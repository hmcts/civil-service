package uk.gov.hmcts.reform.civil.notification.handlers.createlipclaim;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ApplicantClaimSubmittedNotifier;

@Component
public class NotifyApplicantClaimSubmittedNotifier extends Notifier {

    public NotifyApplicantClaimSubmittedNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            NotifyApplicantClaimSubmittedEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return ApplicantClaimSubmittedNotifier.toString();
    }
}
