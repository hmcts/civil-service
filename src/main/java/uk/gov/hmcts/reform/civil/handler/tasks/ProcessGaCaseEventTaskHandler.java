package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Component
public class ProcessGaCaseEventTaskHandler extends BaseExternalTaskHandler {

    public ProcessGaCaseEventTaskHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties
    ) {
        super(externalTaskCompletionService, eventProperties);
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        return new ExternalTaskData();
    }
}
