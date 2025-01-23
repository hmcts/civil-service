package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public abstract class NotificationHandler {

    protected NotificationService notificationService;
    protected NotificationsProperties notificationsProperties;
    protected OrganisationService organisationService;
    protected IStateFlowEngine stateFlowEngine;

    protected abstract void notifyParties(CaseData caseData);

    protected abstract Map<String, String> addProperties(CaseData caseData);

    protected void sendNotification(Set<EmailDTO> recipients) {
        for (EmailDTO recipient : recipients) {
            notificationService.sendMail(recipient.getTargetEmail(), recipient.getEmailTemplate(), recipient.getParameters(),
                    recipient.getReference());
        }
    }
}
