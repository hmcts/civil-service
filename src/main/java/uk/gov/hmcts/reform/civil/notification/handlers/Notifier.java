package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public abstract class Notifier {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    protected final OrganisationService organisationService;
    protected final SimpleStateFlowEngine stateFlowEngine;

    protected Notifier(NotificationService notificationService, NotificationsProperties notificationsProperties,
                       OrganisationService organisationService, SimpleStateFlowEngine stateFlowEngine) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
        this.stateFlowEngine = stateFlowEngine;
    }

    protected void sendNotification(Set<EmailDTO> recipients) {
        for (EmailDTO recipient : recipients) {
            if (Objects.nonNull(recipient.getTargetEmail()) && !recipient.getTargetEmail().isEmpty()) {
                notificationService.sendMail(recipient.getTargetEmail(), recipient.getEmailTemplate(), recipient.getParameters(),
                        recipient.getReference());
            }
        }
    }

    protected abstract void notifyParties(CaseData caseData);

    protected abstract Map<String, String> addProperties(CaseData caseData);
}
