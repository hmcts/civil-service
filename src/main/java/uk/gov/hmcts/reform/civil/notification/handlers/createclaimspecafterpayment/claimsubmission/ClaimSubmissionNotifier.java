package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimsubmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimSubmissionNotifyParties;

@Component
public class ClaimSubmissionNotifier extends Notifier {

    public ClaimSubmissionNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            ClaimSubmissionAllPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return ClaimSubmissionNotifyParties.toString();
    }
}
