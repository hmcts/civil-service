package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

public interface IStateFlowEngine {

    StateFlow evaluate(CaseDetails caseDetails);

    StateFlow evaluate(CaseData caseData);

    StateFlow evaluateSpec(CaseDetails caseDetails);

    StateFlow evaluateSpec(CaseData caseData);

    boolean hasTransitionedTo(CaseDetails caseDetails, FlowState.Main state);
}
