package uk.gov.hmcts.reform.civil.notification.handlers.defresponse.spec.offline.counterclaimordivergedresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseSpecCaseHandedOfflineNotifyParties;

@Component
public class SpecCaseOfflineNotifier
    extends Notifier {

    public SpecCaseOfflineNotifier(NotificationService notificationService,
                                            CaseTaskTrackingService caseTaskTrackingService,
                                            SpecCaseOfflineAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return DefendantResponseSpecCaseHandedOfflineNotifyParties.toString();
    }
}
