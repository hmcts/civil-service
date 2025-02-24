package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.NotificationParty;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.dashboard.entities.NotificationExceptionRecordEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationExceptionRecordRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.model.NotificationParty.APPLICANT_1;
import static uk.gov.hmcts.reform.civil.model.NotificationParty.APPLICANT_2;
import static uk.gov.hmcts.reform.civil.model.NotificationParty.DEFENDANT_1;
import static uk.gov.hmcts.reform.civil.model.NotificationParty.DEFENDANT_2;
import static uk.gov.hmcts.reform.civil.notification.handlers.Notifier.NotificationTask.fromNotificationParty;

@Component
@AllArgsConstructor
public abstract class Notifier implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    protected final OrganisationService organisationService;
    protected final SimpleStateFlowEngine stateFlowEngine;
    protected final NotificationExceptionRecordRepository exceptionRecordRepository;

    protected abstract Set<EmailDTO> getPartiesToNotify(final CaseData caseData);

    public void notifyParties(CaseData caseData, String eventId) {
        final Set<EmailDTO> partiesToEmail = getPartiesToNotify(caseData);
        sendNotification(partiesToEmail, caseData.getCcdCaseReference(), eventId);
    }

    private void sendNotification(Set<EmailDTO> recipients,
                                  Long ccdCaseReference,
                                  String taskId) {
        Optional<NotificationExceptionRecordEntity> existingRecord =
            exceptionRecordRepository.findByReferenceAndTaskId(String.valueOf(ccdCaseReference), taskId);

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
                persistExceptionRecord(ccdCaseReference, existingRecord, successfulNotificationTasks, taskId);
            }
        }
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
                                        String taskId) {
        NotificationExceptionRecordEntity exceptionRecordEntity;

        if (existingRecord.isPresent()) {
            exceptionRecordEntity = existingRecord.get()
                .toBuilder()
                .retryCount(existingRecord.get().getRetryCount() + 1)
                .successfulActions(successfulNotificationTasks.stream().map(Enum::name).toList())
                .updatedOn(OffsetDateTime.now())
                .build();
        } else {
            exceptionRecordEntity = NotificationExceptionRecordEntity.builder()
                .reference(ccdCaseReference.toString())
                .successfulActions(successfulNotificationTasks.stream().map(Enum::name).toList())
                .retryCount(0)
                .taskId(taskId)
                .build();
        }

        exceptionRecordRepository.save(exceptionRecordEntity);
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

