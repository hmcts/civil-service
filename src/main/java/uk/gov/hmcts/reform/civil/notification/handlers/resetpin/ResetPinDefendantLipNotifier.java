package uk.gov.hmcts.reform.civil.notification.handlers.resetpin;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.BaseNotifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.List;

@Component
public class ResetPinDefendantLipNotifier extends BaseNotifier {

    private final ResetPinDefendantLipEmailGenerator partiesNotifier;

    public ResetPinDefendantLipNotifier(NotificationService notificationService,
                                         ResetPinDefendantLipEmailGenerator partiesNotifier) {
        super(notificationService);
        this.partiesNotifier = partiesNotifier;
    }

    public List<String> notifyParties(CaseData caseData) {
        return sendNotification(partiesNotifier.getPartiesToNotify(caseData, null));
    }
}


