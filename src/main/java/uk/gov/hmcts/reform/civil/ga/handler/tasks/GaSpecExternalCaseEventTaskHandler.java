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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;

import java.util.Map;

import static java.util.Optional.ofNullable;

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
        String caseId = ofNullable(variables.getCaseId())
            .orElseThrow(() -> new InvalidCaseDataException("The caseId was not provided"));
        CaseEvent caseEvent = ofNullable(variables.getCaseEvent())
            .orElseThrow(() -> new InvalidCaseDataException("The caseEvent was not provided"));
        log.info("Start GA Event {} for caseId {} activityId {}", caseEvent, caseId, externalTask.getActivityId());

        GeneralApplicationCaseData caseData = processCaseEvent(externalTask, caseId, caseEvent);

        return new ExternalTaskData().setParentCaseData(caseData);
    }

    private GeneralApplicationCaseData processCaseEvent(ExternalTask externalTask, String caseId, CaseEvent caseEvent) {
        StartEventResponse startEventResponse = coreCaseDataService.startGaUpdate(caseId, caseEvent);

        GeneralApplicationCaseData startEventData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = startEventData.getBusinessProcess().copy();

        if (isEventAlreadyProcessed(externalTask, businessProcess)) {
            log.info("GA Event {} for caseId {} activityId {} is already processed",
                     startEventResponse.getEventId(),
                     caseId,
                     externalTask.getActivityId());
            return startEventData;
        }

        businessProcess.setActivityId(externalTask.getActivityId());
        CaseDataContent caseDataContent = gaCaseDataContent(startEventResponse, businessProcess);
        return coreCaseDataService.submitGaUpdate(caseId, caseDataContent);
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        var data = externalTaskData.parentCaseData().orElseThrow();
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
