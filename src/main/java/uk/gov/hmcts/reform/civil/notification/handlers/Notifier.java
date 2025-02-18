package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Set;

@Component
@AllArgsConstructor
public abstract class Notifier implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    protected final OrganisationService organisationService;
    protected final SimpleStateFlowEngine stateFlowEngine;

    protected abstract void notifyParties(CaseData caseData);

    protected void sendNotification(Set<EmailDTO> recipients) {
        for (EmailDTO recipient : recipients) {
            notificationService.sendMail(
                recipient.getTargetEmail(), recipient.getEmailTemplate(), recipient.getParameters(),
                recipient.getReference()
            );
        }
    }
}
