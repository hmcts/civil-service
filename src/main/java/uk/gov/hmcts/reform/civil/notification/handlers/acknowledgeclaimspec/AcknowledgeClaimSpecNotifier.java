package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AcknowledgeSpecClaimNotifier;

@Component
public class AcknowledgeClaimSpecNotifier extends Notifier {

    public AcknowledgeClaimSpecNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            AcknowledgeClaimSpecEmailGenerator allLegalRepsEmailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    protected String getTaskId() {
        return AcknowledgeSpecClaimNotifier.toString();
    }
}
