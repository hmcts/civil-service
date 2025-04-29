package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.LitigationFriendAddedNotifier;

@Component
public class AddDefendantLitigationFriendNotifier
    extends Notifier {

    public AddDefendantLitigationFriendNotifier(NotificationService notificationService,
                                                CaseTaskTrackingService caseTaskTrackingService,
                                                AddDefLitFriendAllLegalRepsEmailGenerator allLegalRepsEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return LitigationFriendAddedNotifier.toString();
    }

}
