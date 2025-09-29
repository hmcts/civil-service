package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedsettledpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantResponseAgreedSettledPartAdmitNotify;

@Component
public class ClaimantResponseAgreedSettledPartAdmitNotifier extends Notifier {

    protected ClaimantResponseAgreedSettledPartAdmitNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                                             ClaimantResponseAgreedSettledPartAdmitPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return ClaimantResponseAgreedSettledPartAdmitNotify.toString();
    }
}
