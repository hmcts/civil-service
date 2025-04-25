package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;

@Component
public class CourtOfficerOrderNotifier extends Notifier {

    private static final String taskId = GenerateOrderNotifyPartiesCourtOfficerOrder.toString();

    public CourtOfficerOrderNotifier(NotificationService notificationService,
                                     CaseTaskTrackingService caseTaskTrackingService,
                                     GenerateOrderCOOAllPartiesEmailGenerator courtOfficeOrderAllPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, courtOfficeOrderAllPartiesEmailGenerator);
        courtOfficeOrderAllPartiesEmailGenerator.setTaskInfo(taskId);
    }

    @Override
    protected String getTaskId() {
        return taskId;
    }
}
