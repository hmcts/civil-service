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
    private final TaskItemContentBuilder taskItemContentBuilder;

    public DashboardScenariosService(NotificationTemplateRepository notificationTemplateRepository,
                                     NotificationRepository notificationRepository,
                                     TaskListService taskListService,
                                     TaskItemTemplateRepository taskItemTemplateRepository,
                                     TaskItemContentBuilder taskItemContentBuilder) {
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.notificationRepository = notificationRepository;
        this.taskListService = taskListService;
        this.taskItemTemplateRepository = taskItemTemplateRepository;
        this.taskItemContentBuilder = taskItemContentBuilder;
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
                .enHTML(stringSubstitutor.replace(template.getEnHTML()))
                .cyHTML(stringSubstitutor.replace(template.getCyHTML()))
                .createdAt(OffsetDateTime.now())
                .build();
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
                    .role(citizenRole)
                    .currentStatus(taskItem.getTaskStatusSequence()[0])
                    .nextStatus(taskItem.getTaskStatusSequence()[1])
                    .taskItemCy(taskItemContentBuilder.buildTaskItemContent(
                        stringSubstitutor,
                        taskItem.getCategoryCy(),
                        taskItem.getContentCy(),
                        taskItem.getTitleCy()
                    ))
                    .taskItemEn(taskItemContentBuilder.buildTaskItemContent(
                        stringSubstitutor,
                        taskItem.getCategoryEn(),
                        taskItem.getContentEn(),
                        taskItem.getTitleEn()
                    ))
                    .build();
                taskListService.saveOrUpdate(taskItemEntity, taskItem.getName());

            });


            //TODO Delete old notifications as notification template says for scenario ref (if exist for case ref)

            List<String> notificationsToBeDeleted = Arrays.asList(template.getNotificationsToBeDeleted());

            notificationsToBeDeleted.forEach(removableTemplate -> {
                Optional<NotificationTemplateEntity> templateToRemove = notificationTemplateRepository
                    .findByName(removableTemplate);
                templateToRemove.ifPresent(t -> {

                    int noOfRowsRemoved = notificationRepository.deleteByNameAndReferenceAndCitizenRole(
                        t.getName(),
                        uniqueCaseIdentifier,
                        t.getRole()
                    );
                    log.info("{} notifications removed for the template {}", noOfRowsRemoved, t.getName());
                });
            });
        });
    }

}
