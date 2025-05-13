package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateDJFormNotifyParties;

@Component
public class GenerateDJFormNotifier extends Notifier {

    private static final String TASK_ID = GenerateDJFormNotifyParties.toString();

    public GenerateDJFormNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        GenerateDJFormAllLegalRepsEmailGenerator generateDJFormAllLegalRepsEmailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, generateDJFormAllLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
