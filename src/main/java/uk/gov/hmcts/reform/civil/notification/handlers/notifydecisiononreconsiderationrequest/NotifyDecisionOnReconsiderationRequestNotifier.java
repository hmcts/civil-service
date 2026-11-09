package uk.gov.hmcts.reform.civil.notification.handlers.notifydecisiononreconsiderationrequest;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class NotifyDecisionOnReconsiderationRequestNotifier extends Notifier<CaseData> {

    public NotifyDecisionOnReconsiderationRequestNotifier(NotificationService notificationService,
                                                          CaseTaskTrackingService caseTaskTrackingService,
                                                          NotifyDecisionOnReconsiderationRequestAllPartiesEmailGenerator partiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, partiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.NotifyDecisionOnReconsiderationRequestNotifier.toString();
    }
}
