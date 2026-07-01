package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondate;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class InformAgreedExtensionDateNotifier extends Notifier {

    public InformAgreedExtensionDateNotifier(NotificationService notificationService,
                                             CaseTaskTrackingService caseTaskTrackingService,
                                             InformAgreedExtensionDateAllPartiesEmailGenerator partiesGenerator) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.InformAgreedExtensionDateNotifier.toString();
    }
}
