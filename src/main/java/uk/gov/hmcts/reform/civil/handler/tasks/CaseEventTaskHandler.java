package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CaseEventTaskHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final StateFlowEngine stateFlowEngine;

    private CaseData data;

    @Override
    public void handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String caseId = variables.getCaseId();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, variables.getCaseEvent());
        CaseData startEventData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = startEventData.getBusinessProcess()
            .updateActivityId(externalTask.getActivityId());

        String flowState = externalTask.getVariable(FLOW_STATE);
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, businessProcess, flowState);
        data = coreCaseDataService.submitUpdate(caseId, caseDataContent);
    }

    @Override
    public VariableMap getVariableMap() {
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(data);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
        return variables;
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse,
                                            BusinessProcess businessProcess,
                                            String flowState) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put("businessProcess", businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId())
                       .summary(getSummary(startEventResponse.getEventId(), flowState))
                       .build())
            .data(data)
            .build();
    }

    private String getSummary(String eventId, String flowState) {
        if (Objects.equals(eventId, CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM.name())) {
            switch (flowState) {
                case "MAIN.FULL_ADMISSION":
                case "MAIN.PART_ADMISSION":
                case "MAIN.COUNTER_CLAIM":
                    return "Proceeding offline due to other defence responses";
                case "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT":
                    return "Proceeding offline due to unrepresented defendant";
                case "MAIN.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT":
                    return "Proceeding offline due to unregistered defendant";
                default:
                    return "Proceeding offline after claimant intention completion";
            }
        }
        return null;
    }
}
