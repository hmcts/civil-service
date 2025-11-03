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
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationProcessCaseEventTaskHandler extends BaseExternalTaskHandler {

    private final CaseDetailsConverter caseDetailsConverter;
    private final GaStateFlowEngine stateFlowEngine;
    private final GaCoreCaseDataService coreCaseDataService;
    private final ObjectMapper mapper;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        log.info("Starting handleTask for ExternalTask ID: {}", externalTask.getId());
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String generalApplicationCaseId = variables.getCaseId();
        StartEventResponse startEventResponse = coreCaseDataService.startGaUpdate(generalApplicationCaseId,
            variables.getCaseEvent());
        CaseData startEventData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = startEventData.getBusinessProcess();
        businessProcess.updateActivityId(externalTask.getActivityId());
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, businessProcess);
        var data = coreCaseDataService.submitGaUpdate(generalApplicationCaseId, caseDataContent);
        log.info("Successfully submitted update for caseId: {}", generalApplicationCaseId);
        return ExternalTaskData.builder().sourceGeneralApplicationCaseData(data).build();
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        var caseData = externalTaskData.sourceGeneralApplicationCaseData().orElseThrow();
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(caseData);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
        variables.putValue("generalAppParentCaseLink", caseData.getGeneralAppParentCaseLink().getCaseReference());
        return variables;
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse,
                                            BusinessProcess businessProcess) {
        Map<String, Object> updatedData = startEventResponse.getCaseDetails().getData();
        updatedData.put("businessProcess", businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId())
                .summary(null)
                .description(null)
                .build())
            .data(updatedData)
            .build();
    }
}
