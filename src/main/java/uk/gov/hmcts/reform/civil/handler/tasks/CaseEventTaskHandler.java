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
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
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

    private String getSummary(String eventId, String state) {
        if (Objects.equals(eventId, CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM.name())) {
            FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state);
            switch (flowState) {
                case FULL_ADMISSION:
                    return "RPA Reason: Defendant fully admits.";
                case PART_ADMISSION:
                    return "RPA Reason: Defendant partial admission.";
                case COUNTER_CLAIM:
                    return "RPA Reason: Defendant rejects and counter claims.";
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT:
                    return "RPA Reason: Unrepresented defendant.";
                case PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT:
                    return "RPA Reason: Unregistered defendant solicitor firm.";
                case FULL_DEFENCE_PROCEED:
                    return "RPA Reason: Applicant proceeds.";
                case FULL_DEFENCE_NOT_PROCEED:
                    return "RPA Reason: Claimant intends not to proceed.";
                case TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED:
                    return "RPA Reason: Only one of the respondent is notified.";
                default:
                    throw new IllegalStateException("Unexpected flow state " + flowState.fullName());
            }
        }
        return null;
    }
}
