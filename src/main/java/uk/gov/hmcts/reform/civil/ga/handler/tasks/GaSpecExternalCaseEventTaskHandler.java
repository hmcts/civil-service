package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class GaSpecExternalCaseEventTaskHandler extends BaseExternalTaskHandler {

    private final GaCoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final GaStateFlowEngine stateFlowEngine;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {

        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String caseId = variables.getCaseId();
        StartEventResponse startEventResponse = coreCaseDataService.startGaUpdate(caseId,
                                                variables.getCaseEvent());
        log.info("Started GA update event for case ID: {} with event: {}", caseId, variables.getCaseEvent());
        GeneralApplicationCaseData startEventData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = startEventData
            .getBusinessProcess().toBuilder()
            .activityId(externalTask.getActivityId()).build();
        CaseDataContent caseDataContent = gaCaseDataContent(startEventResponse, businessProcess);
        var caseData = coreCaseDataService.submitGaUpdate(caseId, caseDataContent);
        return ExternalTaskData.builder().sourceGeneralApplicationCaseData(caseData).build();
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        var data = externalTaskData.sourceGeneralApplicationCaseData().orElseThrow();
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(data);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
        return variables;
    }

    private CaseDataContent gaCaseDataContent(StartEventResponse startGaEventResponse,
                                              BusinessProcess businessProcess) {
        Map<String, Object> objectDataMap = startGaEventResponse.getCaseDetails().getData();
        objectDataMap.put("businessProcess", businessProcess);

        return CaseDataContent.builder()
                .eventToken(startGaEventResponse.getToken())
                .event(Event.builder().id(startGaEventResponse.getEventId())
                        .build())
                .data(objectDataMap)
                .build();
    }
}
