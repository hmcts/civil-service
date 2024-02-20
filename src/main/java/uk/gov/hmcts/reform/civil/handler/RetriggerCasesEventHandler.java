package uk.gov.hmcts.reform.civil.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerCasesEventHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        assert externalTask.getVariable("caseEvent") != null;
        assert externalTask.getVariable("caseIds") != null;

        String caseIds = externalTask.getVariable("caseIds");
        CaseEvent caseEvent = CaseEvent.valueOf(externalTask.getVariable("caseEvent"));

        for (String caseId : caseIds.split(",")) {
            try {
                log.info("Retrigger CaseId: {} started", caseId);
                externalTask.getAllVariables().put("caseId", caseId);
                coreCaseDataService.triggerEvent(Long.parseLong(caseId), caseEvent);
                log.info("Retrigger CaseId: {} finished", caseId);
            } catch (Exception e) {
                log.error("ERROR Retrigger CaseId: {} {}", caseId, e.getMessage(), e);
            }
        }
    }
}
