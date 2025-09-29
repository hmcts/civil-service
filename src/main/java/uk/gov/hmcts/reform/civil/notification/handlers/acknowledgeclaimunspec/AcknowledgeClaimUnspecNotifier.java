package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AcknowledgeClaimUnspecNotifyParties;

@Component
public class AcknowledgeClaimUnspecNotifier
    extends Notifier {

    public AcknowledgeClaimUnspecNotifier(NotificationService notificationService,
                                          CaseTaskTrackingService caseTaskTrackingService,
                                          AcknowledgeClaimUnspecAllLegalRepsEmailGenerator allLegalRepsEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return AcknowledgeClaimUnspecNotifyParties.toString();
    }

}
