package uk.gov.hmcts.reform.civil.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Long.parseLong;
import static java.util.Collections.singletonMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerCasesEventHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper mapper;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        if (externalTask.getVariable("caseEvent") == null) {
            throw new IllegalArgumentException("CaseEvent can not be null");
        }
        if (externalTask.getVariable("caseIds") == null) {
            throw new IllegalArgumentException("CaseIds can not be null");
        }
        String caseIds = externalTask.getVariable("caseIds");
        CaseEvent caseEvent = CaseEvent.valueOf(externalTask.getVariable("caseEvent"));
        String eventSummary = "Re-trigger of " + caseEvent.name();
        String eventDescription = "Process ID: %s".formatted(externalTask.getProcessInstanceId());
        Map<String, Object> caseData = getCaseData(externalTask);
        String orgId = externalTask.getVariable("orgId");

        for (String caseId : caseIds.split(",")) {
            if (orgId != null) {
                log.info("Start setting supplemenary data for CaseId: {}", caseId);
                setSupplementaryData(parseLong(caseId.trim()), orgId);
                log.info("Finish setting supplemenary data for CaseId: {}", caseId);
            } else {
                try {
                    log.info("Retrigger CaseId: {} started", caseId);
                    externalTask.getAllVariables().put("caseId", caseId);
                    coreCaseDataService.triggerEvent(
                        parseLong(caseId.trim()),
                        caseEvent,
                        caseData,
                        eventSummary,
                        eventDescription
                    );
                    log.info("Retrigger CaseId: {} finished. Case data: {}", caseId, caseData);
                } catch (Exception e) {
                    log.error("ERROR Retrigger CaseId: {}. Case data: {},  {}", caseId, caseData, e.getMessage(), e);
                }
            }
        }
        return ExternalTaskData.builder().build();
    }

    private Map<String, Object> getCaseData(ExternalTask externalTask) {
        var typeRef = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        String caseDataString = externalTask.getVariable("caseData");

        if (caseDataString == null || caseDataString.isBlank()) {
            return Map.of();
        }

        try {
            return mapper.readValue(caseDataString, typeRef);
        } catch (Exception e) {
            log.error("Case data could not be deserialized {}", caseDataString, e);
            throw new IllegalArgumentException("Exception deserializing case data", e);
        }
    }

    private void setSupplementaryData(Long caseId, String orgId) {
        Map<String, Map<String, Map<String, Object>>> supplementaryDataCivil = new HashMap<>();
        supplementaryDataCivil.put(
            "supplementary_data_updates",
            singletonMap("$set", singletonMap("orgs_assigned_users", singletonMap(orgId, 1)))
        );
        coreCaseDataService.setSupplementaryData(caseId, supplementaryDataCivil);
    }
}
