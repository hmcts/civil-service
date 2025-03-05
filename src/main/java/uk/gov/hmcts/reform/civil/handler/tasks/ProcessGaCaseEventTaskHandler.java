package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

@RequiredArgsConstructor
@Component
public class ProcessGaCaseEventTaskHandler extends BaseExternalTaskHandler {

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        return ExternalTaskData.builder().build();
    }
}
