package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Component
@Slf4j
public class SimpleStateFlowEngine implements IStateFlowEngine {

    protected final CaseDetailsConverter caseDetailsConverter;
    protected final SimpleStateFlowBuilder stateFlowBuilder;

    @Autowired
    public SimpleStateFlowEngine(CaseDetailsConverter caseDetailsConverter, SimpleStateFlowBuilder stateFlowBuilder) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.stateFlowBuilder = stateFlowBuilder;
    }

    public StateFlow evaluate(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toCaseData(caseDetails));
    }

    public StateFlow evaluate(CaseData caseData) {
        return evaluateFrom(initialStateFor(caseData), caseData);
    }

    public StateFlow evaluateSpec(CaseDetails caseDetails) {
        return evaluateSpec(caseDetailsConverter.toCaseData(caseDetails));
    }

    public StateFlow evaluateSpec(CaseData caseData) {
        return evaluateFrom(FlowState.Main.SPEC_DRAFT, caseData);
    }

    public StateFlowDTO getStateFlow(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toCaseData(caseDetails)).toStateFlowDTO();
    }

    public StateFlowDTO getStateFlow(CaseData caseData) {
        return evaluate(caseData).toStateFlowDTO();
    }

    public StateFlowDTO getStateFlowSpec(CaseDetails caseDetails) {
        StateFlow stateFlow = evaluateSpec(caseDetailsConverter.toCaseData(caseDetails));
        return stateFlow.toStateFlowDTO();
    }

    public StateFlowDTO getStateFlowSpec(CaseData caseData) {
        return evaluateSpec(caseData).toStateFlowDTO();
    }

    public boolean hasTransitionedTo(CaseDetails caseDetails, FlowState.Main state) {
        return evaluate(caseDetails).getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name -> name.equals(state.fullName()));
    }

    private StateFlow evaluateFrom(FlowState.Main initialState, CaseData caseData) {
        return stateFlowBuilder.build(initialState).evaluate(caseData);
    }

    private FlowState.Main initialStateFor(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? FlowState.Main.SPEC_DRAFT
            : FlowState.Main.DRAFT;
    }
}
