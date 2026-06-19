package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

@Component
public class ProcessGaCaseEventTaskHandler extends BaseExternalTaskHandler {

    public ProcessGaCaseEventTaskHandler(
        EventProperties eventProperties
    ) {
        super(eventProperties);
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        return new ExternalTaskData();
    }
}
