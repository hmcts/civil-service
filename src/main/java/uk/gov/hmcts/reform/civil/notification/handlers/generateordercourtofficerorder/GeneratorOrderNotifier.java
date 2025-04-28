package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyParties;

@Component
public class GeneratorOrderNotifier extends Notifier {

    private static final String TASK_ID = GenerateOrderNotifyParties.toString();

    public GeneratorOrderNotifier(NotificationService notificationService,
                                  CaseTaskTrackingService caseTaskTrackingService,
                                  GenerateOrderCOOAllPartiesEmailGenerator generateOrderAllPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, generateOrderAllPartiesEmailGenerator);
        generateOrderAllPartiesEmailGenerator.setTaskInfo(TASK_ID);
    }

    @Override
    protected String getTaskId() {
        return TASK_ID;
    }
}
