package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationUnsuccessfulNotifyParties;

@Component
public class MediationUnsuccessfulPartiesNotifier extends Notifier<CaseData> {

    public MediationUnsuccessfulPartiesNotifier(NotificationService notificationService,
                                                CaseTaskTrackingService caseTaskTrackingService,
                                                MediationUpdateAllPartiesEmailGenerator mediationUnsuccessfulAllPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, mediationUnsuccessfulAllPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return MediationUnsuccessfulNotifyParties.toString();
    }
}
