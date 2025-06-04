package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.TakenOfflineCaseForSpecNotifier;

@Component
public class CaseTakenOfflineForSpecNotifier extends Notifier {

    public CaseTakenOfflineForSpecNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            CaseTakenOfflineForSpecAllPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return TakenOfflineCaseForSpecNotifier.toString();
    }
}
