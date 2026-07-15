package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

import static java.lang.Long.parseLong;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Slf4j
@Component
public class RetriggerUpdateCaseMgmtLocationDataHandler extends BaseExternalTaskHandler {

    private final GaCoreCaseDataService coreCaseDataService;

    public RetriggerUpdateCaseMgmtLocationDataHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        GaCoreCaseDataService coreCaseDataService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.coreCaseDataService = coreCaseDataService;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        if (externalTask.getVariable("caseIds") == null) {
            throw new AssertionError("caseIds is null");
        }
        String epimsId = externalTask.getVariable("ePimId");
        if (epimsId == null) {
            throw new AssertionError("ePimId is null");
        }
        String region = externalTask.getVariable("region");
        if (region == null) {
            throw new AssertionError("region is null");
        }

        String caseIds = externalTask.getVariable("caseIds");
        String eventSummary = "Update Case Management locations epimId by " + epimsId;
        String eventDescription = "Process ID: %s".formatted(externalTask.getProcessInstanceId());

        for (String caseId : caseIds.split(",")) {
            try {
                log.info("Re-trigger update epimsId for CaseId: {} started", caseId);
                externalTask.getAllVariables().put("caseId", caseId);
                coreCaseDataService.triggerUpdateCaseManagementLocation(
                    parseLong(caseId.trim()),
                    CaseEvent.UPDATE_GA_CASE_DATA,
                    region,
                    epimsId,
                    eventSummary,
                    eventDescription
                );
                log.info("Re-trigger update epimsId for CaseId: {} finished. ePimsId: {}", caseId, epimsId);
            } catch (Exception e) {
                log.error("ERROR Re-trigger update epimsId for  CaseId: {}. ePimsId: {},  {}", caseId, epimsId, e.getMessage(), e);
            }
        }
        return new ExternalTaskData();
    }

}
