package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerRegistry;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

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
        String schedulerName = externalTask.getVariable(SCHEDULER_NAME_VARIABLE).toString();
        boolean schedulerFound = schedulerRegistry.runScheduler(schedulerName);

        if (!schedulerFound) {
            log.error("Trigger failed scheduler not found: {}", schedulerName);
        }

        return new ExternalTaskData();
    }
}
