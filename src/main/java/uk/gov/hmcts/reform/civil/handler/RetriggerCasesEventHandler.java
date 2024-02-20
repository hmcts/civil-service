package uk.gov.hmcts.reform.civil.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerCasesEventHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        String caseEventForRetriggerString = externalTask.getVariable("eventForRetrigger");
        
        CaseEvent caseEvent = CaseEvent.fromString(caseEventForRetriggerString);
        String caseIds = externalTask.getVariable("caseIds");
        String[] caseList = ofNullable(caseIds).orElse("").split(",");

        log.info("Attempting to retrigger {} on cases {}.", caseEvent, caseList);
        updateCaseByEvent(asList(caseList), caseEvent);
    }

    public void updateCaseByEvent(List<String> caseIdList, CaseEvent caseEvent) {
        if (caseIdList != null && !caseIdList.isEmpty()) {
            log.info("Retrigger cases started for event: {}", caseEvent);
            caseIdList.forEach(caseId -> {
                try {
                    log.info("Retrigger CaseId: {} started", caseId);
                    coreCaseDataService.triggerEvent(Long.parseLong(caseId), caseEvent);
                    log.info("Retrigger CaseId: {} finished", caseId);
                } catch (Exception e) {
                    log.error("ERROR Retrigger CaseId: {}", caseId);
                    log.error(String.format("Updating case data failed: %s", e.getMessage()));
                    throw e;
                }
                log.info("Retrigger cases Finished for event: {}", caseEvent);
            });
        } else {
            log.info("List id empty for: {}", caseEvent);
        }
    }
}
