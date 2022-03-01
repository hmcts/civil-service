package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StartGeneralApplicationBusinessProcessTaskHandler implements BaseExternalTaskHandler {

    public static final String BUSINESS_PROCESS = "businessProcess";
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final StateFlowEngine stateFlowEngine;

    private VariableMap variables;

    @Override
    public void handleTask(ExternalTask externalTask) {
        CaseData caseData = startGeneralApplicationBusinessProcess(externalTask);
        variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(caseData);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
    }

    @Override
    public VariableMap getVariableMap() {
        return variables;
    }

    private CaseData startGeneralApplicationBusinessProcess(ExternalTask externalTask) {
        ExternalTaskInput externalTaskInput = mapper.convertValue(externalTask.getAllVariables(),
                                                                  ExternalTaskInput.class);
        String caseId = externalTaskInput.getCaseId();
        CaseEvent caseEvent = externalTaskInput.getCaseEvent();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, caseEvent);
        CaseData data = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        List<Element<GeneralApplication>> generalApplications = data.getGeneralApplications();

        Optional<Element<GeneralApplication>> firstGA = generalApplications
            .stream().filter(ga -> ga.getValue() != null
            && ga.getValue().getBusinessProcess() != null
            && StringUtils.isBlank(ga.getValue().getBusinessProcess().getProcessInstanceId())).findFirst();

        if (firstGA.isPresent()) {
            GeneralApplication ga = firstGA.get().getValue();
            switch (ga.getBusinessProcess().getStatusOrDefault()) {
                case READY:
                case DISPATCHED:
                    ga.getBusinessProcess().updateProcessInstanceId(externalTask.getProcessInstanceId());
                    return updateBusinessProcess(caseId, startEventResponse, generalApplications);
                default:
                    throw new BpmnError("ABORT");
            }
        }
        return data;
    }

    private CaseData updateBusinessProcess(
        String ccdId,
        StartEventResponse startEventResponse,
        List<Element<GeneralApplication>> generalApplications
    ) {
        return coreCaseDataService.submitUpdate(ccdId, caseDataContent(startEventResponse, generalApplications));
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse,
                                            List<Element<GeneralApplication>> generalApplications) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put("generalApplications", generalApplications);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }
}
