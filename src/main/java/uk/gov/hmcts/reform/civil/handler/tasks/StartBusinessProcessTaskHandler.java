package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.delegate.BpmnError;
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
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StartBusinessProcessTaskHandler extends BaseExternalTaskHandler {

    public static final String BUSINESS_PROCESS = "businessProcess";
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        CaseData caseData = startBusinessProcess(externalTask);
        var variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.getStateFlow(caseData);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());

        return ExternalTaskData.builder().variables(variables).build();
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        return externalTaskData.getVariables();
    }

    private CaseData startBusinessProcess(ExternalTask externalTask) {
        ExternalTaskInput externalTaskInput = mapper.convertValue(externalTask.getAllVariables(),
                                                                        ExternalTaskInput.class);
        String caseId = externalTaskInput.getCaseId();
        CaseEvent caseEvent = externalTaskInput.getCaseEvent();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, caseEvent);
        CaseData data = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = data.getBusinessProcess();
        switch (businessProcess.getStatusOrDefault()) {
            case READY, DISPATCHED:
                return updateBusinessProcess(caseId, externalTask, startEventResponse, businessProcess);
            case STARTED:
                if (businessProcess.hasSameProcessInstanceId(externalTask.getProcessInstanceId())) {
                    log.error("----------------CAMUNDA SAME PROCESS ID ERROR -START------------------");
                    log.error("CAMUNDAERROR CaseId ({}) CaseEvent ({}) LegacyCaseReference ({}) AllocatedTrack ({})"
                              + "externalTaskProcessInstanceId({}) businessProcessInstanceId({})", caseId,
                              caseEvent, data.getLegacyCaseReference(), data.getAllocatedTrack(),
                              externalTask.getProcessInstanceId(), businessProcess.getProcessInstanceId());
                    log.error("----------------CAMUNDA SAME PROCESS ID ERROR -END------------------");
                    throw new BpmnError("ABORT");
                }
                return data;
            default:
                log.error("----------------CAMUNDAERROR -START------------------");
                log.error("CAMUNDAERROR CaseId ({})", caseId);
                log.error("CAMUNDAERROR CaseEvent ({})", caseEvent);
                log.error("CAMUNDAERROR LegacyCaseReference ({})", data.getLegacyCaseReference());
                log.error("CAMUNDAERROR AllocatedTrack ({})", data.getAllocatedTrack());
                log.error("CAMUNDAERROR BusinessProcessStatus ({})", businessProcess.getStatusOrDefault());
                log.error("----------------CAMUNDAERROR -END------------------");
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
