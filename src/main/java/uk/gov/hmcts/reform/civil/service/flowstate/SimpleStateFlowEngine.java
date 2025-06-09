package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Component
@Slf4j
public class SimpleStateFlowEngine implements IStateFlowEngine {

    protected final CaseDetailsConverter caseDetailsConverter;
    protected final FeatureToggleService featureToggleService;
    protected final SimpleStateFlowBuilder stateFlowBuilder;

    @Autowired
    public SimpleStateFlowEngine(CaseDetailsConverter caseDetailsConverter, FeatureToggleService featureToggleService,
                                 SimpleStateFlowBuilder stateFlowBuilder) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.featureToggleService = featureToggleService;
        this.stateFlowBuilder = stateFlowBuilder;

    }

    @Deprecated
    public StateFlow evaluate(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toCaseData(caseDetails));
    }

    @Deprecated
    public StateFlow evaluate(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return stateFlowBuilder.build(FlowState.Main.SPEC_DRAFT).evaluate(caseData);
        }
        return stateFlowBuilder.build(FlowState.Main.DRAFT).evaluate(caseData);
    }

    @Deprecated
    public StateFlow evaluateSpec(CaseDetails caseDetails) {
        return evaluateSpec(caseDetailsConverter.toCaseData(caseDetails));
    }

    @Deprecated
    public StateFlow evaluateSpec(CaseData caseData) {
        return stateFlowBuilder.build(FlowState.Main.SPEC_DRAFT).evaluate(caseData);
    }

    public StateFlowDTO getStateFlow(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toCaseData(caseDetails)).toStateFlowDTO();
    }

    public StateFlowDTO getStateFlow(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return stateFlowBuilder.build(FlowState.Main.SPEC_DRAFT).evaluate(caseData).toStateFlowDTO();
        }
        return stateFlowBuilder.build(FlowState.Main.DRAFT).evaluate(caseData).toStateFlowDTO();
    }

    public StateFlowDTO getStateFlowSpec(CaseDetails caseDetails) {
        StateFlow stateFlow = evaluateSpec(caseDetailsConverter.toCaseData(caseDetails));
        return stateFlow.toStateFlowDTO();
    }

    public StateFlowDTO getStateFlowSpec(CaseData caseData) {
        return stateFlowBuilder.build(FlowState.Main.SPEC_DRAFT).evaluate(caseData).toStateFlowDTO();
    }

    public boolean hasTransitionedTo(CaseDetails caseDetails, FlowState.Main state) {
        return evaluate(caseDetails).getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name -> name.equals(state.fullName()));
    }
}
