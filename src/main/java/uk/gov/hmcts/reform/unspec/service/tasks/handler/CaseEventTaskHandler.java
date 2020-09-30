package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class CaseEventTaskHandler implements ExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> allVariables = externalTask.getAllVariables();
        String ccdId = (String) allVariables.get("CCD_ID");
        String eventId = (String) allVariables.get("CASE_EVENT");

        coreCaseDataService.triggerEvent(
            Long.valueOf(ccdId),
            CaseEvent.valueOf(eventId),
            Map.of(
                "businessProcess",
                BusinessProcess.builder().activityId(externalTask.getActivityId()).build()
            )
        );
        externalTaskService.complete(externalTask);
    }
}
