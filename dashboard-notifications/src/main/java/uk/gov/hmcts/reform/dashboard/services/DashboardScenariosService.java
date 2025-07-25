package uk.gov.hmcts.reform.dashboard.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.ScenarioEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.ScenarioRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DashboardScenariosService {

    private final ScenarioRepository scenarioRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final TaskItemTemplateRepository taskItemTemplateRepository;

    public DashboardScenariosService(ScenarioRepository scenarioRepository,
                                     NotificationTemplateRepository notificationTemplateRepository,
                                     DashboardNotificationService dashboardNotificationService,
                                     TaskListService taskListService,
                                     TaskItemTemplateRepository taskItemTemplateRepository) {
        this.scenarioRepository = scenarioRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
        this.taskItemTemplateRepository = taskItemTemplateRepository;
    }

    @SuppressWarnings("java:S1172")
    public void recordScenarios(String authorisation, String scenarioReference,
                                String uniqueCaseIdentifier, ScenarioRequestParams scenarioRequestParams) {

        Optional<ScenarioEntity> scenarioByName = scenarioRepository.findByName(scenarioReference);
        scenarioByName.ifPresent(scenario -> {

            //create notifications based on notification template for given scenario Ref.
            createNotificationsForScenario(scenario, uniqueCaseIdentifier, scenarioRequestParams);

            //Create or update taskItem(s) based on task items template for given scenario ref.
            createTaskItemsForScenario(scenarioReference, uniqueCaseIdentifier, scenarioRequestParams);

            //Delete old notifications as notification template says for scenario ref (if exist for case ref)
            deleteNotificationForScenario(scenario, uniqueCaseIdentifier);
        });
    }

    private void createNotificationsForScenario(
        ScenarioEntity scenario, String uniqueCaseIdentifier, ScenarioRequestParams scenarioRequestParams) {

        scenario.getNotificationsToCreate().forEach((templateName, requestParamsKeys) -> {

            Optional<NotificationTemplateEntity> notificationTemplate = notificationTemplateRepository
                .findByName(templateName);

            // build notification eng and wales
            //Supported templates "The ${animal} jumped over the ${target}."
            // "The number is ${undefined.property:-42}."
            List<String> keys =  Arrays.asList(requestParamsKeys);
            notificationTemplate.ifPresent(template -> {
                Map<String, Object> templateParams = scenarioRequestParams.getParams().entrySet().stream()
                    .filter(e -> !keys.isEmpty() && keys.contains(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                StringSubstitutor stringSubstitutor = new StringSubstitutor(templateParams);

                String notificationDeadlineValue = template.getDeadlineParam() != null
                    ? Optional.ofNullable(
                        scenarioRequestParams.getParams().get(template.getDeadlineParam())
                    ).map(Object::toString).orElse(null)
                    : null;
                LocalDateTime notificationDeadline = Optional.ofNullable(notificationDeadlineValue)
                    .map(value -> {
                        try {
                            return LocalDateTime.parse(notificationDeadlineValue);
                        } catch (DateTimeParseException ex) {
                            log.error("Unable to parse deadline for notification {}", notificationDeadlineValue);
                            return null;
                        }
                    })
                    .orElse(null);

                DashboardNotificationsEntity notification = DashboardNotificationsEntity.builder()
                    .id(UUID.randomUUID())
                    .reference(uniqueCaseIdentifier)
                    .name(template.getName())
                    .citizenRole(template.getRole())
                    .dashboardNotificationsTemplates(template)
                    .titleCy(stringSubstitutor.replace(template.getTitleCy()))
                    .titleEn(stringSubstitutor.replace(template.getTitleEn()))
                    .descriptionCy(stringSubstitutor.replace(template.getDescriptionCy()))
                    .descriptionEn(stringSubstitutor.replace(template.getDescriptionEn()))
                    .createdAt(OffsetDateTime.now())
                    .updatedOn(OffsetDateTime.now())
                    .params(scenarioRequestParams.getParams())
                    .deadline(notificationDeadline)
                    .build();

                log.info(
                    "Task Notification details for scenario = {}, template = {}, caseId = {}",
                    scenario.getName(),
                    template.getName(),
                    uniqueCaseIdentifier
                );

                // insert new record in notifications table
                dashboardNotificationService.saveOrUpdate(notification);
            });
        });
    }

    private void createTaskItemsForScenario(
        String scenarioReference, String uniqueCaseIdentifier, ScenarioRequestParams scenarioRequestParams) {

        StringSubstitutor stringSubstitutor = new StringSubstitutor(scenarioRequestParams.getParams());

        List<TaskItemTemplateEntity> taskItemTemplate = taskItemTemplateRepository
            .findByScenarioName(scenarioReference);

        // TaskItemTemplates will have templateName, citizenRole and taskStatusSequence (Minimum two values,
        // if not different, just same value)
        taskItemTemplate.forEach(template -> {

            TaskListEntity taskItemEntity = TaskListEntity.builder()
                .id(UUID.randomUUID())
                .reference(uniqueCaseIdentifier)
                .taskItemTemplate(template)
                .currentStatus(template.getTaskStatusSequence()[0])
                .nextStatus(template.getTaskStatusSequence()[1])
                .taskNameEn(stringSubstitutor.replace(template.getTaskNameEn()))
                .hintTextEn(stringSubstitutor.replace(template.getHintTextEn()))
                .taskNameCy(stringSubstitutor.replace(template.getTaskNameCy()))
                .hintTextCy(stringSubstitutor.replace(template.getHintTextCy()))
                //TODO work on messageParams for specific template
                .messageParams(scenarioRequestParams.getParams())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

            log.info(
                "Task Item details for scenario = {}, id = {}, TaskItemEn = {}, HintTextEn = {}",
                scenarioReference,
                taskItemEntity.getId(),
                taskItemEntity.getTaskNameEn(),
                taskItemEntity.getHintTextEn()
            );
            taskListService.saveOrUpdate(taskItemEntity);
        });
    }

    private void deleteNotificationForScenario(ScenarioEntity scenario, String uniqueCaseIdentifier) {
        Arrays.asList(scenario.getNotificationsToDelete()).forEach(templateName -> {

            Optional<NotificationTemplateEntity> templateToRemove = notificationTemplateRepository
                .findByName(templateName);

            templateToRemove.ifPresent(template -> {
                int noOfRowsRemoved = dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(
                    template.getName(),
                    uniqueCaseIdentifier,
                    template.getRole()
                );
                log.info("{} notifications removed for the template = {}", noOfRowsRemoved, templateName);
            });
        });
    }
}
