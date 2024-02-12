package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class DashboardScenariosService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationRepository notificationRepository;
    private final TaskListService taskListService;
    private final TaskItemTemplateRepository taskItemTemplateRepository;

    public DashboardScenariosService(NotificationTemplateRepository notificationTemplateRepository,
                                     NotificationRepository notificationRepository,
                                     TaskListService taskListService,
                                     TaskItemTemplateRepository taskItemTemplateRepository) {
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.notificationRepository = notificationRepository;
        this.taskListService = taskListService;
        this.taskItemTemplateRepository = taskItemTemplateRepository;
    }

    public void recordScenarios(String authorisation, String scenarioReference,
                                String uniqueCaseIdentifier, ScenarioRequestParams scenarioRequestParams) {
        //TODO create notifications based on notification template for given scenario Ref.

        Optional<NotificationTemplateEntity> notificationTemplate
            = notificationTemplateRepository.findByName(scenarioReference);

        // build notification eng and wales
        //Supported templates "The ${animal} jumped over the ${target}."
        // "The number is ${undefined.property:-42}."
        notificationTemplate.ifPresent(template -> {
            StringSubstitutor stringSubstitutor = new StringSubstitutor(scenarioRequestParams.getParams());

            String citizenRole = template.getRole();
            NotificationEntity notification = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .reference(uniqueCaseIdentifier)
                .name(template.getName())
                .citizenRole(citizenRole)
                .notificationTemplateEntity(template)
                .titleCy(stringSubstitutor.replace(template.getTitleCy()))
                .titleEn(stringSubstitutor.replace(template.getTitleEn()))
                .descriptionCy(stringSubstitutor.replace(template.getDescriptionCy()))
                .descriptionEn(stringSubstitutor.replace(template.getDescriptionEn()))
                .createdAt(OffsetDateTime.now())
                .build();

            log.info(
                "Task Notification details for scenario {}, id = {}, enHTML = {}, cyHTML = {}",
                scenarioReference,
                notification.getId(),
                notification.getTitleCy(),
                notification.getDescriptionEn()
            );

            // insert new record in notifications table
            notificationRepository.save(notification);

            //TODO Create or update taskItem(s) based on task items template for given scenario ref.

            List<TaskItemTemplateEntity> taskList = taskItemTemplateRepository
                .findByNameAndRole(scenarioReference, citizenRole);

            // TaskItemTemplates will have templateName, citizenRole and taskStatusSequence (Minimum two values,
            // if not different, just same value)
            taskList.forEach(taskItem -> {
                TaskListEntity taskItemEntity = TaskListEntity.builder()
                    .id(UUID.randomUUID())
                    .reference(uniqueCaseIdentifier)
                    .taskItemTemplate(taskItem)
                    .currentStatus(taskItem.getTaskStatusSequence()[0])
                    .nextStatus(taskItem.getTaskStatusSequence()[1])
                    .taskNameEn(stringSubstitutor.replace(taskItem.getTaskNameEn()))
                    .hintTextEn(stringSubstitutor.replace(taskItem.getHintTextEn()))
                    .taskNameCy(stringSubstitutor.replace(taskItem.getTaskNameCy()))
                    .hintTextCy(stringSubstitutor.replace(taskItem.getHintTextCy()))

                    .build();
                log.info(
                    "Task Item details for scenario {}, id = {}, TaskItemEn = {}, TaskItemCy = {}",
                    scenarioReference,
                    taskItemEntity.getId(),
                    taskItemEntity.getTaskNameEn(),
                    taskItemEntity.getHintTextEn()
                );
                taskListService.saveOrUpdate(taskItemEntity, taskItem.getName());

            });

            //TODO Delete old notifications as notification template says for scenario ref (if exist for case ref)

            List<String> notificationsToBeDeleted = Arrays.asList(template.getNotificationsToBeDeleted());

            notificationsToBeDeleted.forEach(removableTemplate -> {
                log.info("Removing notifications for the template {}", removableTemplate);

                Optional<NotificationTemplateEntity> templateToRemove = notificationTemplateRepository
                    .findByName(removableTemplate);

                templateToRemove.ifPresent(t -> {
                    int noOfRowsRemoved = notificationRepository.deleteByNameAndReferenceAndCitizenRole(
                        t.getName(),
                        uniqueCaseIdentifier,
                        t.getRole()
                    );
                    log.info("{} notifications removed for the template {}", noOfRowsRemoved, removableTemplate);
                });
            });
        });
    }
}
