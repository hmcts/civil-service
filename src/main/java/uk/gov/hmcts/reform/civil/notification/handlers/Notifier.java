package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@AllArgsConstructor
public abstract class Notifier {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    protected final OrganisationService organisationService;
    protected final SimpleStateFlowEngine stateFlowEngine;

    protected void sendNotification(Set<EmailDTO> recipients) {
        for (EmailDTO recipient : recipients) {
            notificationService.sendMail(recipient.getTargetEmail(), recipient.getEmailTemplate(), recipient.getParameters(),
                recipient.getReference());
        }
    }

    public void notifyParties(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));
        sendNotification(partiesToEmail);
    }

    protected abstract Map<String, String> addProperties(CaseData caseData);

    protected abstract EmailDTO getApplicant(CaseData caseData);

    protected abstract Set<EmailDTO> getRespondents(CaseData caseData);
}
