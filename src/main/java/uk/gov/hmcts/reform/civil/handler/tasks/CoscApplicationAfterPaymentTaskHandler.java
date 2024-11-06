package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.exception.ValueMapperException;
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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@RequiredArgsConstructor
@Component
public class CoscApplicationAfterPaymentTaskHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper mapper;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public ExternalTaskData  handleTask(ExternalTask externalTask) {
        try {
            ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);

            String civilCaseId =
                ofNullable(variables.getGeneralAppParentCaseLink())
                    .orElseThrow(() -> new InvalidCaseDataException(
                        "General application parent case link not found"));
            CaseEvent caseEvent = ofNullable(variables.getCaseEvent())
                .orElseThrow(() -> new InvalidCaseDataException("The case event was not provided"));

            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(civilCaseId, caseEvent);

            var data = coreCaseDataService.submitUpdate(civilCaseId, caseDataContentFromStartEventResponse(startEventResponse, Map.of()));
            return ExternalTaskData.builder().caseData(data).build();

        } catch (NumberFormatException ne) {
            throw new InvalidCaseDataException(
                "Conversion to long datatype failed for general application for a case ", ne
            );
        } catch (IllegalArgumentException | ValueMapperException e) {
            throw new InvalidCaseDataException("Mapper conversion failed due to incompatible types", e);
        }
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        var data = externalTaskData.caseData().orElseThrow();
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(data);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
        variables.putValue("isJudgmentMarkedPaidInFull", checkMarkPaidInFull(data));
        variables.putValue("isClaimantLR", isClaimantLR(data));
        return variables;
    }

    private boolean checkMarkPaidInFull(CaseData data) {
        return (Objects.nonNull(data.getActiveJudgment()) && (data.getActiveJudgment().getFullyPaymentMadeDate() != null));
    }

    private boolean isClaimantLR(CaseData caseData) {
        return YES.equals(caseData.getApplicant1Represented());
    }
}
