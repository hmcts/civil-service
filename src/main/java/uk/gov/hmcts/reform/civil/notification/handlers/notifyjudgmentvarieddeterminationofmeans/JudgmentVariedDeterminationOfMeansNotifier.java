package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.JudgmentVariedDeterminationOfMeansNotifyParties;

@Component
public class JudgmentVariedDeterminationOfMeansNotifier extends Notifier<CaseData> {

    public JudgmentVariedDeterminationOfMeansNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            JudgmentVariedDeterminationOfMeansAllPartiesEmailGenerator partiesGen
    ) {
        super(notificationService, caseTaskTrackingService, partiesGen);
    }

    @Override
    protected String getTaskId() {
        return JudgmentVariedDeterminationOfMeansNotifyParties.toString();
    }
}
