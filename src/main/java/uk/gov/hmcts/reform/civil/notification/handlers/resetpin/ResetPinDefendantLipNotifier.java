package uk.gov.hmcts.reform.civil.notification.handlers.resetpin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.BaseNotifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.List;

@Component
@Slf4j
public class ResetPinDefendantLipNotifier extends BaseNotifier {

    private final ResetPinDefendantLipEmailGenerator partiesNotifier;

    public ResetPinDefendantLipNotifier(NotificationService notificationService,
                                         ResetPinDefendantLipEmailGenerator partiesNotifier) {
        super(notificationService);
        this.partiesNotifier = partiesNotifier;
    }

    public List<String> notifyParties(CaseData caseData) {
        log.info("Reset PIN Notifying parties for case id: {}", caseData.getCcdCaseReference());
        return sendNotification(partiesNotifier.getPartiesToNotify(caseData, null));
    }
}


