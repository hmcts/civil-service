package uk.gov.hmcts.reform.civil.notification.handlers.judgmentbyadmission;

import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import org.springframework.stereotype.Component;

@Component
public class JudgmentByAdmissionNotifier extends Notifier {

    public JudgmentByAdmissionNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        JudgmentByAdmissionAllPartiesEmailGenerator partiesGenerator
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.JudgmentByAdmissionNotifier.toString();
    }
}
