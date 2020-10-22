package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;
import uk.gov.hmcts.reform.unspec.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.stateflow.StateFlow;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StartBusinessProcessTaskHandler implements ExternalTaskHandler {

    public static final String FLOW_STATE = "flowState";
    public static final String BUSINESS_PROCESS = "businessProcess";
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final StateFlowEngine stateFlowEngine;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        CaseData caseData = startBusinessProcess(externalTask);
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        externalTaskService.complete(externalTask, variables);
    }

    private CaseData startBusinessProcess(ExternalTask externalTask) {
        Map<String, Object> allVariables = externalTask.getAllVariables();
        ExternalTaskInput externalTaskInput = objectMapper.convertValue(allVariables, ExternalTaskInput.class);
        String caseId = externalTaskInput.getCaseId();
        CaseEvent caseEvent = externalTaskInput.getCaseEvent();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, caseEvent);
        CaseData data = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = data.getBusinessProcess();

        switch (businessProcess.getStatusOrDefault()) {
            case READY:
            case DISPATCHED:
                return updateBusinessProcess(caseId, externalTask, startEventResponse, businessProcess);
            case STARTED:
                if (businessProcess.hasSameProcessInstanceId(externalTask.getProcessInstanceId())) {
                    throw new BpmnError("ABORT");
                }
                return data;
            default:
                throw new BpmnError("ABORT");
        }
    }

    private CaseData updateBusinessProcess(
        String ccdId,
        ExternalTask externalTask,
        StartEventResponse startEventResponse,
        BusinessProcess businessProcess
    ) {
        businessProcess = businessProcess.updateProcessInstanceId(externalTask.getProcessInstanceId());
        return coreCaseDataService.submitUpdate(ccdId, caseDataContent(startEventResponse, businessProcess));
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, BusinessProcess businessProcess) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put(BUSINESS_PROCESS, businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }
}
