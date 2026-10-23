package uk.gov.hmcts.reform.civil.notification.handlers.notifylipgenerictemplate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class NotifyLipGenericTemplateNotifier extends Notifier<CaseData> {

    public NotifyLipGenericTemplateNotifier(NotificationService notificationService,
                                            CaseTaskTrackingService caseTaskTrackingService,
                                            NotifyLipGenericTemplateAllPartiesEmailGenerator allPartiesGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesGenerator);
    }

    @Override
    public String getTaskId() {
        return CamundaProcessIdentifier.NotifyLipGenericTemplateNotifier.toString();
    }
}
