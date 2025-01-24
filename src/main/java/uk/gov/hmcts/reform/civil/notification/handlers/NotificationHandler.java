package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor(force = true)
public abstract class NotificationHandler {

    protected static final String REFERENCE_TEMPLATE_APPLICANT = "litigation-friend-added-applicant-notification-%s";
    protected static final String REFERENCE_TEMPLATE_RESPONDENT = "litigation-friend-added-respondent-notification-%s";
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

    protected abstract void notifyParties(CaseData caseData);

    protected abstract Map<String, String> addProperties(CaseData caseData);
}
