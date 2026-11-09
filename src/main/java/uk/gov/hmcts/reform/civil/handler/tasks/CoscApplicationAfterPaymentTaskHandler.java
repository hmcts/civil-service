package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Component
public class CoscApplicationAfterPaymentTaskHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper mapper;
    private final IStateFlowEngine stateFlowEngine;

    public CoscApplicationAfterPaymentTaskHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CoreCaseDataService coreCaseDataService,
        ObjectMapper mapper,
        IStateFlowEngine stateFlowEngine
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.coreCaseDataService = coreCaseDataService;
        this.mapper = mapper;
        this.stateFlowEngine = stateFlowEngine;
    }

    @Override
    public ExternalTaskData  handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = toExternalTaskInput(externalTask, mapper);

        String civilCaseId =
            ofNullable(variables.getGeneralAppParentCaseLink())
                .orElseThrow(() -> new InvalidCaseDataException(
                    "General application parent case link not found"));
        CaseEvent caseEvent = ofNullable(variables.getCaseEvent())
            .orElseThrow(() -> new InvalidCaseDataException("The case event was not provided"));

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(civilCaseId, caseEvent);

        var data = coreCaseDataService.submitUpdate(civilCaseId, caseDataContentFromStartEventResponse(startEventResponse, Map.of()));
        return new ExternalTaskData().setCaseData(data);
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        var data = externalTaskData.caseData().orElseThrow();
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.getStateFlow(data);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
        variables.putValue("isJudgmentMarkedPaidInFull", checkMarkPaidInFull(data));
        variables.putValue("isClaimantLR", !data.isApplicant1NotRepresented());
        return variables;
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }

    private boolean checkMarkPaidInFull(CaseData data) {
        return (Objects.nonNull(data.getActiveJudgment()) && (data.getActiveJudgment().getFullyPaymentMadeDate() != null));
    }

}
