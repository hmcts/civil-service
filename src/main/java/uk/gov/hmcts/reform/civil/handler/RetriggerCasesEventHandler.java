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

import java.util.Map;

import static java.lang.Long.parseLong;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerCasesEventHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper mapper;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        assert externalTask.getVariable("caseEvent") != null;
        assert externalTask.getVariable("caseIds") != null;

        String caseIds = externalTask.getVariable("caseIds");
        CaseEvent caseEvent = CaseEvent.valueOf(externalTask.getVariable("caseEvent"));
        String eventSummary = "Re-trigger of " + caseEvent.name();
        String eventDescription = "Process ID: %s".formatted(externalTask.getProcessInstanceId());
        Map<String, Object> caseData = getCaseData(externalTask);

        for (String caseId : caseIds.split(",")) {
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
}
