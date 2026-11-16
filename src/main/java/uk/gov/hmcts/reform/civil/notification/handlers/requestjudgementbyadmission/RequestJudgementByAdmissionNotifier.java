package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.RequestJudgementByAdmissionNotifyParties;

@Component
public class RequestJudgementByAdmissionNotifier extends Notifier<CaseData> {

    protected RequestJudgementByAdmissionNotifier(
        NotificationService notificationService,
        CaseTaskTrackingService caseTaskTrackingService,
        RequestJudgementByAdmissionAllPartiesEmailGenerator partiesNotifier) {
        super(notificationService, caseTaskTrackingService, partiesNotifier);
    }

    @Override
    protected String getTaskId() {
        return RequestJudgementByAdmissionNotifyParties.toString();
    }
}
