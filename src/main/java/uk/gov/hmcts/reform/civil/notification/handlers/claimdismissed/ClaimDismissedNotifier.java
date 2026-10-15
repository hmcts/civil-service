package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimDismissedNotifyParties;

@Component
public class ClaimDismissedNotifier
    extends Notifier<CaseData> {

    public ClaimDismissedNotifier(NotificationService notificationService,
                                  CaseTaskTrackingService caseTaskTrackingService,
                                  ClaimDismissedAllLegalRepsEmailGenerator allLegalRepsEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return ClaimDismissedNotifyParties.toString();
    }

}
