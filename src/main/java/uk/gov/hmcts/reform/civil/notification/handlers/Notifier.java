package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.dashboard.entities.NotificationExceptionRecordEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationExceptionRecordRepository;

import java.util.Set;
import java.util.UUID;

@Component
@AllArgsConstructor
public abstract class Notifier implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    protected final OrganisationService organisationService;
    protected final SimpleStateFlowEngine stateFlowEngine;
    protected final NotificationExceptionRecordRepository exceptionRecordRepository;

    protected abstract Set<EmailDTO> getPartiesToNotify(final CaseData caseData);

    public void notifyParties(CaseData caseData) {
        final Set<EmailDTO> partiesToEmail = getPartiesToNotify(caseData);
        sendNotification(partiesToEmail, caseData.getCcdCaseReference());
    }

    private void sendNotification(Set<EmailDTO> recipients, Long ccdCaseReference) {
        for (EmailDTO recipient : recipients) {
            try {
                notificationService.sendMail(
                    recipient.getTargetEmail(), recipient.getEmailTemplate(), recipient.getParameters(),
                    recipient.getReference()
                );
            } catch (Exception e) {
                final NotificationExceptionRecordEntity notificationExceptionRecordEntity = NotificationExceptionRecordEntity.builder()
                    .id(UUID.randomUUID())
                    .reference(ccdCaseReference.toString())
                    .build();

                exceptionRecordRepository.save(notificationExceptionRecordEntity);
            }
        }
    }
}

