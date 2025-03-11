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
        if (externalTask.getVariable("caseIds") == null) {
            throw new AssertionError("caseIds is null");
        }
        String epimsId = externalTask.getVariable("ePimId");
        if (epimsId == null) {
            throw new AssertionError("ePimId is null");
        }
        String caseIds = externalTask.getVariable("caseIds");
        String eventSummary = "Update locations epimId by " + epimsId;
        String eventDescription = "Process ID: %s".formatted(externalTask.getProcessInstanceId());

        for (String caseId : caseIds.split(",")) {
            try {
                log.info("Re-trigger update epimsId for CaseId: {} started", caseId);
                externalTask.getAllVariables().put("caseId", caseId);
                coreCaseDataService.triggerUpdateLocationEpimdsIdEvent(
                    parseLong(caseId.trim()),
                    CaseEvent.UPDATE_CASE_DATA,
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
