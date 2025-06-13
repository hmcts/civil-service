package uk.gov.hmcts.reform.civil.notification.handlers.hearingfeeunpaid;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.UnpaidHearingFeeNotifier;

@Component
public class HearingFeeUnpaidHearingFeeNotifier extends Notifier {

    public HearingFeeUnpaidHearingFeeNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            HearingFeeUnpaidAllPartiesEmailGenerator allPartiesEmailGenerator
    ) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return UnpaidHearingFeeNotifier.toString();
    }
}
