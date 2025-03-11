package uk.gov.hmcts.reform.civil.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static java.lang.Long.parseLong;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerUpdateLocationDataHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        if (externalTask.getVariable("caseEvent") == null)  {
            throw new AssertionError();
        }
        if (externalTask.getVariable("caseIds") == null) {
            throw new AssertionError();
        }
        if (externalTask.getVariable("ePimsId") == null) {
            throw new AssertionError();
        }

        String caseIds = externalTask.getVariable("caseIds");
        CaseEvent caseEvent = CaseEvent.valueOf(externalTask.getVariable("caseEvent"));
        String eventSummary = "Re-trigger of " + caseEvent.name();
        String eventDescription = "Process ID: %s".formatted(externalTask.getProcessInstanceId());
        String epimsId = externalTask.getVariable("ePimsId");

        for (String caseId : caseIds.split(",")) {
            try {
                log.info("Re-trigger update epimsId for CaseId: {} started", caseId);
                externalTask.getAllVariables().put("caseId", caseId);
                coreCaseDataService.triggerUpdateLocationEpimdsIdEvent(
                    parseLong(caseId.trim()),
                    caseEvent,
                    epimsId,
                    eventSummary,
                    eventDescription
                );
                log.info("Re-trigger update epimsId for CaseId: {} finished. ePimsId: {}", caseId, epimsId);
            } catch (Exception e) {
                log.error("ERROR Re-trigger update epimsId for  CaseId: {}. ePimsId: {},  {}", caseId, epimsId, e.getMessage(), e);
            }
        }
        return ExternalTaskData.builder().build();
    }
}
