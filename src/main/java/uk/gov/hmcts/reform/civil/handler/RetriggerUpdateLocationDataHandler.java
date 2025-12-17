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
        String caseIds = externalTask.getVariable("caseIds");
        if (caseIds == null) {
            throw new IllegalArgumentException("caseIds is null");
        }
        String epimsId = externalTask.getVariable("ePimId");
        if (epimsId == null) {
            throw new IllegalArgumentException("ePimId is null");
        }
        String transferReason = externalTask.getVariable("reason");
        if (transferReason == null) {
            throw new IllegalArgumentException("transferReason is null");
        }
        String eventSummary = "Updated case management location with epimId " + epimsId;
        String eventDescription = "Updated case management location with epimId " + epimsId;

        for (String caseId : caseIds.split(",")) {
            try {
                log.info("Re-trigger update case management location with epimsId for CaseId: {} started", caseId);
                externalTask.getAllVariables().put("caseId", caseId);
                coreCaseDataService.triggerUpdateCaseMgmtLocation(
                    parseLong(caseId.trim()),
                    CaseEvent.TRANSFER_ONLINE_CASE,
                    epimsId,
                    transferReason,
                    eventSummary,
                    eventDescription
                );
                log.info("Re-trigger update case management location with epimsId for CaseId: {} finished. ePimsId: {}",
                         caseId, epimsId);
            } catch (Exception e) {
                log.error("ERROR Re-trigger update case management location with epimsId for  CaseId: {}. ePimsId: {},  {}",
                          caseId, epimsId, e.getMessage(), e);
            }
        }
        return new ExternalTaskData();
    }
}
