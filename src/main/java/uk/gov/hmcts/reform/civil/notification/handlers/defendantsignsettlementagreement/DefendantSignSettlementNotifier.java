package uk.gov.hmcts.reform.civil.notification.handlers.defendantsignsettlementagreement;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantSignSettlementNotify;

@Component
public class DefendantSignSettlementNotifier extends Notifier<CaseData> {

    public DefendantSignSettlementNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                           DefendantSignSettlementPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return DefendantSignSettlementNotify.toString();
    }
}
