package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.raisingclaimagainstlitigantinperson;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.RaisingClaimAgainstSpecLitigantInPersonForNotifier;

@Component
public class RaisingClaimAgainstLitigantInPersonForSpecNotifier extends Notifier {

    public RaisingClaimAgainstLitigantInPersonForSpecNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            RaisingClaimAgainstLitigantInPersonForSpecAllPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return RaisingClaimAgainstSpecLitigantInPersonForNotifier.toString();
    }
}
