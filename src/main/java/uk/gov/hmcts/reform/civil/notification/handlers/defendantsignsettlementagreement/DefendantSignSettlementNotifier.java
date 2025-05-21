package uk.gov.hmcts.reform.civil.notification.handlers.defendantsignsettlementagreement;

import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantSignSettlementNotify;

public class DefendantSignSettlementNotifier extends Notifier {
    public DefendantSignSettlementNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                           DefendantSignSettlementLipvLipEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return DefendantSignSettlementNotify.toString();
    }
}
