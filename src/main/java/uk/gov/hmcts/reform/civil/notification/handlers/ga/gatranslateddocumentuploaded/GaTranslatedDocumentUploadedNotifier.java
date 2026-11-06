package uk.gov.hmcts.reform.civil.notification.handlers.ga.gatranslateddocumentuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class GaTranslatedDocumentUploadedNotifier extends Notifier<GeneralApplicationCaseData> {

    public static final String TASK_ID = "NotifyGaTranslatedDocumentUploaded";

    public GaTranslatedDocumentUploadedNotifier(NotificationService notificationService,
                                                CaseTaskTrackingService caseTaskTrackingService,
                                                GaTranslatedDocumentUploadedAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return TASK_ID;
    }
}
