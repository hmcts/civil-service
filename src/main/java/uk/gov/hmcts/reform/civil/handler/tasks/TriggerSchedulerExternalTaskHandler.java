package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerRegistry;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
public class TriggerSchedulerExternalTaskHandler extends BaseExternalTaskHandler {

    private static final String SCHEDULER_NAME_VARIABLE = "schedulerName";

    private final SchedulerRegistry schedulerRegistry;

    protected TriggerSchedulerExternalTaskHandler(ExternalTaskCompletionService externalTaskCompletionService,
                                                  EventProperties eventProperties,
                                                  SchedulerRegistry schedulerRegistry) {
        super(externalTaskCompletionService, eventProperties);
        this.schedulerRegistry = schedulerRegistry;
    }

    @Override
    protected ExternalTaskData handleTask(ExternalTask externalTask) {
        String schedulerName = externalTask.getVariable(SCHEDULER_NAME_VARIABLE);

        if (!hasText(schedulerName)) {
            log.error("Trigger scheduler failed: '{}' variable not set", SCHEDULER_NAME_VARIABLE);
            return new ExternalTaskData();
        }

        if (!schedulerRegistry.runScheduler(schedulerName)) {
            log.error("Trigger scheduler failed: scheduler not found for name '{}'", schedulerName);
        }

        return new ExternalTaskData();
    }
}
