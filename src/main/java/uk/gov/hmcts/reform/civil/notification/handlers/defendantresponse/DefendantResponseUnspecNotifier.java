package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend.AddDefLitFriendAllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseUnspecFullDefenceNotifyParties;

@Component
public class DefendantResponseUnspecNotifier
    extends Notifier {

    public DefendantResponseUnspecNotifier(NotificationService notificationService,
                                           CaseTaskTrackingService caseTaskTrackingService,
                                           AddDefLitFriendAllLegalRepsEmailGenerator allLegalRepsEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return DefendantResponseUnspecFullDefenceNotifyParties.toString();
    }

}
