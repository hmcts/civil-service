package uk.gov.hmcts.reform.civil.handler.tasks;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import static java.util.Optional.ofNullable;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

@Component
@Slf4j
public class PaymentTaskHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final IStateFlowEngine stateFlowEngine;

    public PaymentTaskHandler(
        EventProperties eventProperties,
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter,
        ObjectMapper objectMapper,
        IStateFlowEngine stateFlowEngine
    ) {
        super(eventProperties);
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.objectMapper = objectMapper;
        this.stateFlowEngine = stateFlowEngine;
    }

    public ExternalTaskData handleTask(ExternalTask externalTask) {
        try {
            Map<String, Object> allVariables = externalTask.getAllVariables();
            ExternalTaskInput externalTaskInput = objectMapper.convertValue(allVariables, ExternalTaskInput.class);
            String caseId = ofNullable(externalTaskInput.getCaseId())
                .orElseThrow(() -> new InvalidCaseDataException("The caseId was not provided"));
            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
                caseId,
                externalTaskInput.getCaseEvent()
            );
            BusinessProcess businessProcess = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())
                .getBusinessProcess().updateActivityId(externalTask.getActivityId());
            var data = coreCaseDataService.submitUpdate(caseId, caseDataContent(startEventResponse, businessProcess));
            return new ExternalTaskData().setCaseData(data);
        } catch (ValueMapperException | IllegalArgumentException e) {
            throw new InvalidCaseDataException("Mapper conversion failed due to incompatible types", e);
        }
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        var data = externalTaskData.caseData().orElseThrow();
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.getStateFlow(data);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
        return variables;
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, BusinessProcess businessProcess) {
        Map<String, Object> caseData = startEventResponse.getCaseDetails().getData();
        caseData.put("businessProcess", businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(caseData)
            .build();
    }
}
