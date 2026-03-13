package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.STANDARD_DIRECTION_ORDER_DJ_NOTIFY_PARTIES;

@Component
public class StandardDirectionOrderDJNotifier extends Notifier {

    private static final String TASK_ID = STANDARD_DIRECTION_ORDER_DJ_NOTIFY_PARTIES.toString();

    public StandardDirectionOrderDJNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        StandardDirectionOrderDJAllPartiesEmailGenerator standardDirectionOrderDJAllPartiesEmailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, standardDirectionOrderDJAllPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
