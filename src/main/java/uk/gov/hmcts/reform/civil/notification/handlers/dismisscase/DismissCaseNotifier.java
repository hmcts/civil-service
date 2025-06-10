package uk.gov.hmcts.reform.civil.notification.handlers.dismisscase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
@Slf4j
public class DismissCaseNotifier extends Notifier {

    public DismissCaseNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService, DismissCaseAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.DismissCaseNotifier.toString();
    }
}
