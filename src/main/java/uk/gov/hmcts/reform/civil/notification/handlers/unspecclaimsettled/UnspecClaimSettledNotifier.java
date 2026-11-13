package uk.gov.hmcts.reform.civil.notification.handlers.unspecclaimsettled;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

@Component
public class UnspecClaimSettledNotifier extends Notifier<CaseData> {

    public UnspecClaimSettledNotifier(NotificationService notificationService,
                                      CaseTaskTrackingService caseTaskTrackingService,
                                      UnspecClaimSettledAllPartiesEmailGenerator allPartiesEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailGenerator);
    }

    @Override
    protected String getTaskId() {
        return CamundaProcessIdentifier.UnspecClaimSettledNotifier.toString();
    }
}
