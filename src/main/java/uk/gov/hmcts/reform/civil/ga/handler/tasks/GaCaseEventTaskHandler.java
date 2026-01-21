package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class GaCaseEventTaskHandler extends BaseExternalTaskHandler {

    private final GaCoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final GaStateFlowEngine stateFlowEngine;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String caseId = variables.getCaseId();
        log.info("Starting case event task for case ID: {}, event: {}", caseId, variables.getCaseEvent());
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId,
                                                                                variables.getCaseEvent());
        CaseData startEventData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = startEventData
            .getBusinessProcess().toBuilder()
            .activityId(externalTask.getActivityId()).build();
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, businessProcess);
        var data = coreCaseDataService.submitUpdate(caseId, caseDataContent);
        return ExternalTaskData.builder().parentCaseData(data).build();
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        var data = externalTaskData.parentCaseData().orElseThrow();
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(data);
        var stateFlowName = stateFlow.getState().getName();
        var stateFlags = stateFlow.getFlags();
        variables.putValue(FLOW_STATE, stateFlowName);
        variables.putValue(FLOW_FLAGS, stateFlags);
        log.debug("Evaluated state flow for case, flow state: {}, flags: {}", stateFlowName, stateFlags);
        return variables;
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse,
                                            BusinessProcess businessProcess) {
        Map<String, Object> updatedData = startEventResponse.getCaseDetails().getData();
        updatedData.put("businessProcess", businessProcess);

        log.debug("Preparing case data content for event ID: {}", startEventResponse.getEventId());
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId())
                       .summary(getSummary())
                       .description(getDescription())
                       .build())
            .data(updatedData)
            .build();
    }

    private String getSummary() {

        return null;
    }

    private String getDescription() {

        return null;
    }
}
