package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.HearingNoticeGeneratorHMCNotifier;

@Component
public class GenerateHearingNoticeHMCNotifier extends Notifier {

    protected GenerateHearingNoticeHMCNotifier(NotificationService notificationService, CaseTaskTrackingService caseTaskTrackingService,
                                               GenerateHearingNoticeHMCAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    public String getTaskId() {
        return HearingNoticeGeneratorHMCNotifier.toString();
    }
}
