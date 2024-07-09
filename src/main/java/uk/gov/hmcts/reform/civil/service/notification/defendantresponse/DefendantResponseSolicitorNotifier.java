package uk.gov.hmcts.reform.civil.service.notification.defendantresponse;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;

public abstract class DefendantResponseSolicitorNotifier implements NotificationData {


    public void notifySolicitorForDefendantResponse(CaseData caseData) {
        String recipient;
        recipient = getRecipient(caseData);
        sendNotificationToSolicitor(caseData, recipient);

    }

    protected abstract String getRecipient(CaseData caseData);

    protected abstract void sendNotificationToSolicitor(CaseData caseData, String recipient);

}
