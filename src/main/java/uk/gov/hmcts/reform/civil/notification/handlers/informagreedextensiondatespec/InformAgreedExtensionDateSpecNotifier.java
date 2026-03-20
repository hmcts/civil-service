package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class InformAgreedExtensionDateSpecNotifier extends Notifier {

    public InformAgreedExtensionDateSpecNotifier(NotificationService notificationService,
                                                 CaseTaskTrackingService caseTaskTrackingService,
                                                 InformAgreedExtensionDateSpecAllPartiesEmailGenerator partiesGenerator) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.InformAgreedExtensionDateSpecNotifier.toString();
    }
}
