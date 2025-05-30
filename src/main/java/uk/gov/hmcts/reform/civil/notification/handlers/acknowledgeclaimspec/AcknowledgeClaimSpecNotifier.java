package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AcknowledgeClaimSpecNotifyParties;

@Component
public class AcknowledgeClaimSpecNotifier extends Notifier {

    public AcknowledgeClaimSpecNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            AcknowledgeClaimSpecAllLegalRepsEmailGenerator allLegalRepsEmailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return AcknowledgeClaimSpecNotifyParties.toString();
    }
}