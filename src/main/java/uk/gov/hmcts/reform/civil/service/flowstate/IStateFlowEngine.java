package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.Map;

public interface IStateFlowEngine {

    StateFlow evaluate(CaseDetails caseDetails);

    StateFlow evaluate(CaseData caseData);

    StateFlow evaluateSpec(CaseDetails caseDetails);

    StateFlow evaluateSpec(CaseData caseData);

    StateFlowDTO getStateFlow(CaseDetails caseDetails);

    StateFlowDTO getStateFlow(CaseData caseData);

    StateFlowDTO getStateFlowSpec(CaseDetails caseDetails);

    StateFlowDTO getStateFlowSpec(CaseData caseData);

    boolean hasTransitionedTo(CaseDetails caseDetails, FlowState.Main state);
}
