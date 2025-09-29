package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@Component
public class MediationSuccessfulPartiesNotifier extends Notifier {

    public MediationSuccessfulPartiesNotifier(NotificationService notificationService,
                                  CaseTaskTrackingService caseTaskTrackingService,
                                  MediationUpdateAllPartiesEmailGenerator mediationSuccessfulAllPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, mediationSuccessfulAllPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return MediationSuccessfulNotifyParties.toString();
    }
}
