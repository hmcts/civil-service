package uk.gov.hmcts.reform.civil.stateflow.transitions;

import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

public abstract class MidTransitionBuilder extends TransitionBuilder {

    public MidTransitionBuilder(FlowState.Main fromState, FeatureToggleService featureToggleService) {
        super(fromState, featureToggleService);
    }
}
