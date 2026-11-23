package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateDJFormNotifyParties;

@Component
public class GenerateDJFormNotifier extends Notifier<CaseData> {

    private static final String TASK_ID = GenerateDJFormNotifyParties.toString();

    public GenerateDJFormNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        GenerateDJFormAllPartiesEmailGenerator generateDJFormAllPartiesEmailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, generateDJFormAllPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
