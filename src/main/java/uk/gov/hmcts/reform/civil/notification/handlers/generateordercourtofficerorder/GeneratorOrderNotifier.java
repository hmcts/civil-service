package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyParties;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;

@Component
public class GeneratorOrderNotifier extends Notifier {

    private static final String taskId = GenerateOrderNotifyParties.toString();

    public GeneratorOrderNotifier(NotificationService notificationService,
                                  CaseTaskTrackingService caseTaskTrackingService,
                                  GenerateOrderCOOAllPartiesEmailGenerator generateOrderAllPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, generateOrderAllPartiesEmailGenerator);
        generateOrderAllPartiesEmailGenerator.setTaskInfo(taskId);
    }

    @Override
    protected String getTaskId() {
        return GenerateOrderNotifyParties.toString();
    }
}
