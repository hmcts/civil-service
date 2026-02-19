package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
@Slf4j
public class DjNonDivergentNotifier extends Notifier {

    public DjNonDivergentNotifier(NotificationService notificationService,
                                   CaseTaskTrackingService caseTaskTrackingService,
                                   DjNonDivergentAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.DjNonDivergentNotifier.toString();
    }
}
