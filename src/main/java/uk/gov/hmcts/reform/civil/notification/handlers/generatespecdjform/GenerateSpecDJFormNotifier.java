package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class GenerateSpecDJFormNotifier extends Notifier {

    public GenerateSpecDJFormNotifier(NotificationService notificationService,
                                      CaseTaskTrackingService caseTaskTrackingService,
                                      GenerateSpecDJFormAllPartiesEmailGenerator partiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, partiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.GenerateSpecDJFormNotifier.toString();
    }
}
