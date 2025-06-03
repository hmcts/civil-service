package uk.gov.hmcts.reform.civil.notification.handlers.amendrestitchbundle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AmendRestitchBundleNotify;

@Component
@Slf4j
public class AmendRestitchBundleNotifier extends Notifier {

    public AmendRestitchBundleNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                       AmendRestitchBundleAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return AmendRestitchBundleNotify.toString();
    }

}
