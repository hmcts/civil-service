package uk.gov.hmcts.reform.civil.notification.handlers.translateddocumentuploaded;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
@Slf4j
public class TranslatedDocumentUploadedNotifier extends Notifier {

    public TranslatedDocumentUploadedNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        TranslatedDocumentUploadedAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.TranslatedDocumentUploadedNotifyParties.toString();
    }
}
