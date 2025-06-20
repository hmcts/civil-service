package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class HearingProcessNotifier extends Notifier {

    public HearingProcessNotifier(NotificationService notificationService,
                                  CaseTaskTrackingService caseTaskTrackingService,
                                  HearingProcessAllPartiesEmailGenerator partiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, partiesEmailGenerator);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.HearingProcessNotifier.toString();
    }
}
