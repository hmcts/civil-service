package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.offline.otherresponses;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseUnspecCaseHandedOfflineNotifyParties;

@Component
public class DefRespCaseOfflineUnspecNotifier
    extends Notifier {

    public DefRespCaseOfflineUnspecNotifier(NotificationService notificationService,
                                            CaseTaskTrackingService caseTaskTrackingService,
                                            DefRespCaseOfflineAllLegalRepsEmailGenerator allLegalRepsEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return DefendantResponseUnspecCaseHandedOfflineNotifyParties.toString();
    }
}
