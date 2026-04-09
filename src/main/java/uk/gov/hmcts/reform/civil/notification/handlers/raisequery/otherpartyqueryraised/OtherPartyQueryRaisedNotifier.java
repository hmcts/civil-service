package uk.gov.hmcts.reform.civil.notification.handlers.raisequery.otherpartyqueryraised;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class OtherPartyQueryRaisedNotifier extends Notifier {

    public OtherPartyQueryRaisedNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        OtherPartyQueryRaisedAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    protected String getTaskId() {
        return  CamundaProcessIdentifier.OtherPartyQueryRaisedNotifier.toString();
    }
}
