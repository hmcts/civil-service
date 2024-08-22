package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Service
public class CaseDismissClaimantNotificationHandler extends CaseDismissNotificationHandler {

    public CaseDismissClaimantNotificationHandler(NotificationService notificationService,
                                                  NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    protected String getPartyName(CaseData caseData) {
        return caseData.getApplicant1().getPartyName();
    }
}
