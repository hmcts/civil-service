package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.nocSubmittedForLiPDefendant;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC;

@Component
public class SpecDefendantNocTransitionBuilder extends MidTransitionBuilder {

    public SpecDefendantNocTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.SPEC_DEFENDANT_NOC, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(TAKEN_OFFLINE_SPEC_DEFENDANT_NOC).onlyWhen(nocSubmittedForLiPDefendant);
    }

}
