package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskDTO;
import uk.gov.hmcts.reform.civil.model.NotificationParty;
import uk.gov.hmcts.reform.civil.notification.EmailNotificationFailedException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.dashboard.entities.NotificationExceptionId;
import uk.gov.hmcts.reform.dashboard.entities.NotificationExceptionRecordEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationExceptionRecordRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.model.NotificationParty.APPLICANT_1;
import static uk.gov.hmcts.reform.civil.model.NotificationParty.APPLICANT_2;
import static uk.gov.hmcts.reform.civil.model.NotificationParty.DEFENDANT_1;
import static uk.gov.hmcts.reform.civil.model.NotificationParty.DEFENDANT_2;
import static uk.gov.hmcts.reform.civil.notification.handlers.Notifier.NotificationTask.fromNotificationParty;

@Component
@AllArgsConstructor
@Slf4j
public abstract class Notifier implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    protected final OrganisationService organisationService;
    protected final SimpleStateFlowEngine stateFlowEngine;
    protected final NotificationExceptionRecordRepository exceptionRecordRepository;
    private final CamundaRuntimeClient camundaClient;

    protected abstract Set<EmailDTO> getPartiesToNotify(final CaseData caseData);

    public void notifyParties(CaseData caseData, String eventId) {
        final Set<EmailDTO> partiesToEmail = getPartiesToNotify(caseData);
        sendNotification(partiesToEmail,
                         caseData.getCcdCaseReference(),
                         eventId,
                         caseData.getBusinessProcess().getProcessInstanceId());
    }

    private void sendNotification(Set<EmailDTO> recipients,
                                  Long ccdCaseReference,
                                  String taskId,
                                  String processInstanceId) {
        NotificationExceptionId notificationExceptionId = new NotificationExceptionId(String.valueOf(ccdCaseReference), taskId);
        Optional<NotificationExceptionRecordEntity> existingRecord =
            exceptionRecordRepository.findNotificationExceptionRecordEntitiesByNotificationExceptionId(notificationExceptionId);

        List<NotificationTask> successfulNotificationTasks = getSuccessfulNotifications(existingRecord);

        for (EmailDTO recipient : recipients) {
            List<NotificationParty> partiesSuccessfullyEmailed = successfulNotificationTasks.stream()
                .map(NotificationTask::getNotificationParty).toList();

            if (partiesSuccessfullyEmailed.contains(recipient.getParty())) {
                continue;
            }

            try {
                notificationService.sendMail(
                    recipient.getTargetEmail(), recipient.getEmailTemplate(), recipient.getParameters(),
                    recipient.getReference()
                );

                successfulNotificationTasks.add(fromNotificationParty(recipient.getParty()));
            } catch (Exception e) {
                log.error("Error sending email for case {} with error {}", ccdCaseReference, e.getMessage());
            }
        }

        if (successfulNotificationTasks.size() != recipients.size()) {
            persistExceptionRecord(ccdCaseReference, existingRecord, successfulNotificationTasks, taskId, processInstanceId);

            String recipientList = recipients.stream()
                .map(EmailDTO::getParty).map(Enum::name)
                .collect(Collectors.joining(", "));

            String partiesSuccessfullyEmailed = successfulNotificationTasks.stream()
                .map(NotificationTask::getNotificationParty).map(Enum::name)
                .collect(Collectors.joining(", "));

            String errorMessage = "Failed to send email for eventId %s for case %s with recipient list %s and successful emails sent %s"
                .formatted(taskId, ccdCaseReference, recipientList, partiesSuccessfullyEmailed);
            throw new EmailNotificationFailedException(errorMessage);
        }

        exceptionRecordRepository.deleteByNotificationExceptionId(notificationExceptionId);
    }

    @NotNull
    private List<NotificationTask> getSuccessfulNotifications(Optional<NotificationExceptionRecordEntity> existingRecord) {

        List<NotificationTask> successfulNotificationTasks = new ArrayList<>();

        existingRecord
            .ifPresent(notificationExceptionRecordEntity -> notificationExceptionRecordEntity.getSuccessfulActions()
                .forEach(successfulAction -> {
                    NotificationTask task = NotificationTask.valueOf(successfulAction);
                    successfulNotificationTasks.add(task);
                }));
        return successfulNotificationTasks;
    }

    private void persistExceptionRecord(Long ccdCaseReference,
                                        Optional<NotificationExceptionRecordEntity> existingRecord,
                                        List<NotificationTask> successfulNotificationTasks,
                                        String taskId,
                                        String processInstanceId) {
        NotificationExceptionRecordEntity exceptionRecordEntity;
        int retryCount = getRetryCount(processInstanceId);

        if (existingRecord.isPresent()) {
            exceptionRecordEntity = existingRecord.get()
                .toBuilder()
                .retryCount(retryCount)
                .successfulActions(successfulNotificationTasks.stream().map(Enum::name).toList())
                .updatedOn(OffsetDateTime.now())
                .build();
        } else {
            OffsetDateTime createdAt = OffsetDateTime.now();
            exceptionRecordEntity = NotificationExceptionRecordEntity.builder()
                .notificationExceptionId(
                    NotificationExceptionId.builder()
                        .reference(ccdCaseReference.toString())
                        .taskId(taskId)
                        .build()
                )
                .successfulActions(successfulNotificationTasks.stream().map(Enum::name).toList())
                .retryCount(retryCount)
                .createdAt(createdAt)
                .updatedOn(createdAt)
                .build();
        }

        exceptionRecordRepository.save(exceptionRecordEntity);
    }

    private int getRetryCount(String processInstanceId) {
        List<ExternalTaskDTO> externalTasks = camundaClient.getTasksForProcessInstance(processInstanceId);

        if (externalTasks == null || externalTasks.size() != 1) {
            return 0;
        }

        return externalTasks.get(0).getRetries();
    }

    protected enum NotificationTask {
        EMAIL_APPLICANT_ONE(APPLICANT_1),
        EMAIL_APPLICANT_TWO(APPLICANT_2),
        EMAIL_DEFENDANT_ONE(DEFENDANT_1),
        EMAIL_DEFENDANT_TWO(DEFENDANT_2);

        private final NotificationParty notificationParty;

        NotificationTask(NotificationParty notificationParty) {
            this.notificationParty = notificationParty;
        }

        public NotificationParty getNotificationParty() {
            return notificationParty;
        }

        public static NotificationTask fromNotificationParty(NotificationParty party) {
            for (NotificationTask task : NotificationTask.values()) {
                if (task.getNotificationParty() == party) {
                    return task;
                }
            }
            throw new IllegalArgumentException("No matching NotificationTask for party: " + party);
        }
    }
}
