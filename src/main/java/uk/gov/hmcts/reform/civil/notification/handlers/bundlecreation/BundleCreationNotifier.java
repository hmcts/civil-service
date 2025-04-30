package uk.gov.hmcts.reform.civil.notification.handlers.bundlecreation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.BundleCreationNotify;

@Component
public class BundleCreationNotifier extends Notifier {

    public BundleCreationNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                  BundleCreationPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return BundleCreationNotify.toString();
    }

}
