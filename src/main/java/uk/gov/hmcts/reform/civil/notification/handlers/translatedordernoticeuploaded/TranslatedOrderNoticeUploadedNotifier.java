package uk.gov.hmcts.reform.civil.notification.handlers.translatedordernoticeuploaded;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
@Slf4j
public class TranslatedOrderNoticeUploadedNotifier extends Notifier {

    public TranslatedOrderNoticeUploadedNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        TranslatedOrderNoticeUploadedAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.TranslatedOrderNoticeUploadedNotifyParties.toString();
    }
}
