package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForSpec;

@Component
public class RaisingClaimAgainstLitigantInPersonForSpecNotifier extends Notifier {

    public RaisingClaimAgainstLitigantInPersonForSpecNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            RaisingClaimAgainstLitigantInPersonForSpecPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForSpec.toString();
    }
}
