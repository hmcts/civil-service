package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;

public abstract class FullDefenceSolicitorNotifier implements NotificationData {

    protected static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    public void notifySolicitorForDefendantResponse(CaseData caseData) {
        sendNotificationToSolicitor(caseData, getRecipient(caseData));

    }

    protected abstract String getRecipient(CaseData caseData);

    protected abstract void sendNotificationToSolicitor(CaseData caseData, String recipient);

}
