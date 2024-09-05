package uk.gov.hmcts.reform.civil.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static java.lang.Long.parseLong;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerCaseFlagEventHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        if (externalTask.getVariable("caseIds") == null) {
            throw new AssertionError();
        }

        String caseIds = externalTask.getVariable("caseIds");
        CaseEvent caseEvent = CaseEvent.UPDATE_CASE_DATA;
        String eventSummary = "Re-trigger of " + caseEvent.name();
        String eventDescription = "Process ID: %s".formatted(externalTask.getProcessInstanceId());

        for (String caseId : caseIds.split(",")) {
            try {
                log.info("Re-trigger update case flag for CaseId: {} started", caseId);
                externalTask.getAllVariables().put("caseId", caseId);
                coreCaseDataService.updateCaseFlagEvent(
                    parseLong(caseId.trim()),
                    caseEvent,
                    eventSummary,
                    eventDescription
                );
                log.info("Re-trigger update case flag CaseId: {} finished.", caseId);
            } catch (Exception e) {
                log.error("ERROR in update Re-trigger CaseId: {}.  {}", caseId, e.getMessage(), e);
            }
        }
    }
}
