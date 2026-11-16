package uk.gov.hmcts.reform.civil.notification.handlers.setasidejudgementrequest;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class SetAsideJudgementRequestNotifier extends Notifier<CaseData> {

    public SetAsideJudgementRequestNotifier(NotificationService notificationService,
                                            CaseTaskTrackingService caseTaskTrackingService,
                                            SetAsideJudgementRequestAllPartiesEmailGenerator partiesGenerator) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.SetAsideJudgementRequestNotifier.toString();
    }
}
