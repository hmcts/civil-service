package uk.gov.hmcts.reform.civil.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static java.lang.Long.parseLong;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerUpdateLocationDataHandler extends BaseExternalTaskHandler {

    protected static final String CASE_IDS = "caseIds";
    protected static final String CASE_IDS_CSV_FILENAME = "caseIdsCsvFilename";
    protected static final String EPIM_ID = "ePimId";
    private final CoreCaseDataService coreCaseDataService;
    private final CaseReferenceCsvLoader caseReferenceCsvLoader;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        if (externalTask.getVariable(CASE_IDS) == null && externalTask.getVariable(CASE_IDS_CSV_FILENAME) == null) {
            throw new AssertionError("caseIds or caseIdsFileName is null");
        }

        String epimsId = externalTask.getVariable(EPIM_ID);
        if (epimsId == null) {
            throw new AssertionError("ePimId is null");
        }

        String caseManagementLocation = externalTask.getVariable("caseManagementLocation");
        String courtLocation = externalTask.getVariable("courtLocation");
        String applicant1DQRequestedCourt = externalTask.getVariable("applicant1DQRequestedCourt");
        String region = externalTask.getVariable("region");

        String[] caseIds = getCaseIds(externalTask);
        String eventSummary = "Update locations epimId by " + epimsId;
        String eventDescription = "Process ID: %s".formatted(externalTask.getProcessInstanceId());

        for (String caseId : caseIds) {
            try {
                log.info("Re-trigger update epimsId for CaseId: {} started", caseId);
                externalTask.getAllVariables().put("caseId", caseId);
                coreCaseDataService.triggerUpdateLocationEpimdsIdEvent(
                    parseLong(caseId.trim()),
                    CaseEvent.UPDATE_CASE_DATA,
                    epimsId,
                    region,
                    caseManagementLocation,
                    courtLocation,
                    applicant1DQRequestedCourt,
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

    private String[] getCaseIds(ExternalTask externalTask) {
        String[] caseIds;
        if (externalTask.getVariable(CASE_IDS) != null) {
            String caseIdsString = externalTask.getVariable(CASE_IDS);
            caseIds = caseIdsString.split(",");
        } else {
            String csvFileName = externalTask.getVariable(CASE_IDS_CSV_FILENAME);
            try {
                caseIds = caseReferenceCsvLoader.loadCaseReferenceList(csvFileName)
                    .stream()
                    .map(caseReferenceKeyValue -> caseReferenceKeyValue.getCaseReference())
                    .toArray(String[]::new);
            } catch (RuntimeException e) {
                log.error("Failed to load case references from CSV file: {}", csvFileName, e);
                caseIds = new String[0];
            }
        }
        return caseIds;
    }
}
