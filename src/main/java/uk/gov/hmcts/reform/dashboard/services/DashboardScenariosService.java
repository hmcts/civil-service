package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.ScenarioEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.ScenarioRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;
import uk.gov.hmcts.reform.dashboard.templates.NotificationTemplateCatalog;
import uk.gov.hmcts.reform.dashboard.templates.NotificationTemplateDefinition;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DashboardScenariosService {

    private static final String RECORD_SCENARIO_DURATION_METRIC = "dashboard.scenario.record.duration_ms";
    private static final String RECORD_SCENARIO_EVENT = "dashboard.scenario.record";

    private final ScenarioRepository scenarioRepository;
    private final NotificationTemplateCatalog notificationTemplateCatalog;
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final TaskItemTemplateRepository taskItemTemplateRepository;
    private final TelemetryService telemetryService;

    @Value("${dashboard.scenario.slow-threshold:15000}")
    private long slowScenarioStepThresholdMs;

    public DashboardScenariosService(ScenarioRepository scenarioRepository,
                                     NotificationTemplateCatalog notificationTemplateCatalog,
                                     DashboardNotificationService dashboardNotificationService,
                                     TaskListService taskListService,
                                     TaskItemTemplateRepository taskItemTemplateRepository,
                                     TelemetryService telemetryService) {
        this.scenarioRepository = scenarioRepository;
        this.notificationTemplateCatalog = notificationTemplateCatalog;
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
        this.taskItemTemplateRepository = taskItemTemplateRepository;
        this.telemetryService = telemetryService;
    }

    @SuppressWarnings("java:S1172")
    public void recordScenarios(String authorisation, String scenarioReference,
                                String uniqueCaseIdentifier, ScenarioRequestParams scenarioRequestParams) {
        long started = System.nanoTime();
        boolean success = false;
        boolean scenarioFound = false;
        log.info("Recording scenario {} with caseReference {}", scenarioReference, uniqueCaseIdentifier);

        try {
            Optional<ScenarioEntity> scenarioByName = timedStep(
                "lookupScenario",
                scenarioReference,
                uniqueCaseIdentifier,
                () -> scenarioRepository.findByName(scenarioReference)
            );
            scenarioFound = scenarioByName.isPresent();
            scenarioByName.ifPresent(scenario -> {

                //create notifications based on notification template for given scenario Ref.
                timedStep("createNotifications", scenarioReference, uniqueCaseIdentifier,
                          () -> createNotificationsForScenario(scenario, uniqueCaseIdentifier, scenarioRequestParams));

                //Create or update taskItem(s) based on task items template for given scenario ref.
                timedStep("createTaskItems", scenarioReference, uniqueCaseIdentifier,
                          () -> createTaskItemsForScenario(scenarioReference, uniqueCaseIdentifier, scenarioRequestParams));

                //Delete old notifications as notification template says for scenario ref (if exist for case ref)
                timedStep("deleteNotifications", scenarioReference, uniqueCaseIdentifier,
                          () -> deleteNotificationForScenario(scenario, uniqueCaseIdentifier));
            });
            success = true;
        } finally {
            long durationMs = elapsedMs(started);
            trackRecordScenarioTelemetry(
                scenarioReference,
                uniqueCaseIdentifier,
                "total",
                durationMs,
                success,
                scenarioFound
            );
            logScenarioDuration("total", scenarioReference, uniqueCaseIdentifier, durationMs, success);
        }
    }

    public void reconfigureCaseDashboardNotifications(String uniqueCaseIdentifier, ScenarioRequestParams scenarioRequestParams, String roleType) {
        dashboardNotificationService.getDashboardNotifications(uniqueCaseIdentifier, roleType)
            .forEach(existingNotification -> reconfigureCaseNotificationTemplate(existingNotification, uniqueCaseIdentifier, scenarioRequestParams));
    }

    private void reconfigureCaseNotificationTemplate(DashboardNotificationsEntity existingNotification, String uniqueCaseIdentifier, ScenarioRequestParams scenarioRequestParams) {
        Optional<NotificationTemplateDefinition> optionalTemplate =
            notificationTemplateCatalog.findByName(existingNotification.getName());

        if (optionalTemplate.isEmpty()) {
            log.warn("Template not found for notification {}", existingNotification.getName());
            return;
        }

        NotificationTemplateDefinition template = optionalTemplate.get();

        HashMap<String, Object> params = scenarioRequestParams.getParams();
        StringSubstitutor substitutor = new StringSubstitutor(params);

        LocalDateTime notificationDeadline = null;
        String deadlineParam = template.getDeadlineParam();
        if (deadlineParam != null) {
            Object raw = params.get(deadlineParam);
            if (raw != null) {
                try {
                    notificationDeadline = LocalDateTime.parse(raw.toString());
                } catch (DateTimeParseException ex) {
                    log.error("Unable to parse deadline '{}' for notification {}",
                              raw, template.getName());
                }
            }
        }

        DashboardNotificationsEntity updated = new DashboardNotificationsEntity(
            existingNotification.getId(),
            existingNotification.getReference(),
            existingNotification.getName(),
            existingNotification.getCitizenRole(),
            substitutor.replace(template.getTitleEn()),
            substitutor.replace(template.getDescriptionEn()),
            substitutor.replace(template.getTitleCy()),
            substitutor.replace(template.getDescriptionCy()),
            params,
            existingNotification.getCreatedBy(),
            existingNotification.getCreatedAt(),
            existingNotification.getUpdatedBy(),
            OffsetDateTime.now(),
            notificationDeadline,
            template.getTimeToLive()
        );

        dashboardNotificationService.saveOrUpdate(updated);

        log.info("Reconfigured notification {} for case {}",
                 template.getName(), uniqueCaseIdentifier);
    }

    private void createNotificationsForScenario(
        ScenarioEntity scenario, String uniqueCaseIdentifier, ScenarioRequestParams scenarioRequestParams) {

        scenario.getNotificationsToCreate().forEach((templateName, requestParamsKeys) -> {

            Optional<NotificationTemplateDefinition> notificationTemplate = notificationTemplateCatalog
                .findByName(templateName)
                .filter(template -> {
                    if (template.isMarkedForDeletion()) {
                        log.info("Skipping notification template {} because it is marked for deletion", templateName);
                        return false;
                    }
                    return true;
                });

            // build notification eng and wales
            //Supported templates "The ${animal} jumped over the ${target}."
            // "The number is ${undefined.property:-42}."
            List<String> keys = Arrays.asList(requestParamsKeys);
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

                DashboardNotificationsEntity notification = new DashboardNotificationsEntity(
                    UUID.randomUUID(),
                    uniqueCaseIdentifier,
                    template.getName(),
                    template.getRole(),
                    stringSubstitutor.replace(template.getTitleEn()),
                    stringSubstitutor.replace(template.getDescriptionEn()),
                    stringSubstitutor.replace(template.getTitleCy()),
                    stringSubstitutor.replace(template.getDescriptionCy()),
                    scenarioRequestParams.getParams(),
                    null,
                    OffsetDateTime.now(),
                    null,
                    OffsetDateTime.now(),
                    notificationDeadline,
                    template.getTimeToLive()
                );

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
        log.info("Creating task items for scenario {} with caseReference {}", scenarioReference, uniqueCaseIdentifier);

        StringSubstitutor stringSubstitutor = new StringSubstitutor(scenarioRequestParams.getParams());

        List<TaskItemTemplateEntity> taskItemTemplate = taskItemTemplateRepository
            .findByScenarioName(scenarioReference);

        // TaskItemTemplates will have templateName, citizenRole and taskStatusSequence (Minimum two values,
        // if not different, just same value)
        taskItemTemplate.forEach(template -> {

            TaskListEntity taskItemEntity = new TaskListEntity(
                UUID.randomUUID(),
                template,
                uniqueCaseIdentifier,
                template.getTaskStatusSequence()[0],
                template.getTaskStatusSequence()[1],
                stringSubstitutor.replace(template.getTaskNameEn()),
                stringSubstitutor.replace(template.getHintTextEn()),
                stringSubstitutor.replace(template.getTaskNameCy()),
                stringSubstitutor.replace(template.getHintTextCy()),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                scenarioRequestParams.getParams()
            );

            log.info(
                "Task Item details for the role: {}, scenario = {}, caseReference = {}, id = {}, TaskItemEn = {}, HintTextEn = {}," +
                    "CurrentStatus = {}, nextStatus = {}",
                template.getRole(),
                scenarioReference,
                uniqueCaseIdentifier,
                taskItemEntity.getId(),
                taskItemEntity.getTaskNameEn(),
                taskItemEntity.getHintTextEn(),
                taskItemEntity.getCurrentStatus(),
                taskItemEntity.getNextStatus()
            );
            taskListService.saveOrUpdate(taskItemEntity);
        });
    }

    private void deleteNotificationForScenario(ScenarioEntity scenario, String uniqueCaseIdentifier) {
        Arrays.asList(scenario.getNotificationsToDelete()).forEach(templateName -> {

            Optional<NotificationTemplateDefinition> templateToRemove = notificationTemplateCatalog.findByName(templateName);

            if (templateToRemove.isPresent()) {
                NotificationTemplateDefinition template = templateToRemove.get();
                int noOfRowsRemoved = dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(
                    template.getName(),
                    uniqueCaseIdentifier,
                    template.getRole()
                );
                log.info("{} notifications removed for the template = {}", noOfRowsRemoved, templateName);
            } else {
                int removed = dashboardNotificationService.deleteByNameAndReference(templateName, uniqueCaseIdentifier);
                log.info(
                    "{} notifications removed for the template = {} without role information",
                    removed,
                    templateName
                );
            }
        });
    }

    private <T> T timedStep(String step, String scenarioReference, String uniqueCaseIdentifier, TimedSupplier<T> supplier) {
        long started = System.nanoTime();
        boolean success = false;
        try {
            T result = supplier.get();
            success = true;
            return result;
        } finally {
            long durationMs = elapsedMs(started);
            trackRecordScenarioTelemetry(scenarioReference, uniqueCaseIdentifier, step, durationMs, success, null);
            logScenarioDuration(step, scenarioReference, uniqueCaseIdentifier, durationMs, success);
        }
    }

    private void timedStep(String step, String scenarioReference, String uniqueCaseIdentifier, Runnable runnable) {
        timedStep(step, scenarioReference, uniqueCaseIdentifier, () -> {
            runnable.run();
            return null;
        });
    }

    private long elapsedMs(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private void logScenarioDuration(String step, String scenarioReference, String uniqueCaseIdentifier,
                                     long durationMs, boolean success) {
        if (durationMs >= slowScenarioStepThresholdMs) {
            log.warn(
                "Slow dashboard scenario record step={} scenario={} caseReference={} durationMs={} success={}",
                step,
                scenarioReference,
                uniqueCaseIdentifier,
                durationMs,
                success
            );
            return;
        }

        log.info(
            "Dashboard scenario record step={} scenario={} caseReference={} durationMs={} success={}",
            step,
            scenarioReference,
            uniqueCaseIdentifier,
            durationMs,
            success
        );
    }

    private void trackRecordScenarioTelemetry(String scenarioReference, String uniqueCaseIdentifier, String step,
                                              long durationMs, boolean success, Boolean scenarioFound) {
        telemetryService.trackMetric(RECORD_SCENARIO_DURATION_METRIC, durationMs);

        Map<String, String> properties = new HashMap<>();
        properties.put("scenario", scenarioReference);
        properties.put("caseReference", uniqueCaseIdentifier);
        properties.put("step", step);
        properties.put("durationMs", String.valueOf(durationMs));
        properties.put("success", String.valueOf(success));
        if (scenarioFound != null) {
            properties.put("scenarioFound", String.valueOf(scenarioFound));
        }

        telemetryService.trackEvent(RECORD_SCENARIO_EVENT, properties);
    }

    @FunctionalInterface
    private interface TimedSupplier<T> {
        T get();
    }
}
