package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsepartadmitpayimmediately;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class ClaimantResponsePartAdmitPayImmediatelyNotifier extends Notifier<CaseData> {

    protected ClaimantResponsePartAdmitPayImmediatelyNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                              ClaimantResponsePartAdmitPayImmediatelyAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.ClaimantResponsePartAdmitPayImmediatelyNotifier.toString();
    }
}
