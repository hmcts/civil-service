package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;

import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DiscontinuanceClaimNotifyParties;

@Component
public class DiscontinueClaimPartiesNotifier extends Notifier {

    private static final String TASK_ID = DiscontinuanceClaimNotifyParties.toString();

    public DiscontinueClaimPartiesNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        DiscontinueClaimPartiesAllPartiesEmailGenerator discontinueClaimPartiesAllPartiesEmailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, discontinueClaimPartiesAllPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
