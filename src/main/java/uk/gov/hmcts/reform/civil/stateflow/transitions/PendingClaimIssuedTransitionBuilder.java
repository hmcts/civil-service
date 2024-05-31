package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;

@Component
public class PendingClaimIssuedTransitionBuilder extends MidTransitionBuilder {
    public PendingClaimIssuedTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.PENDING_CLAIM_ISSUED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(CLAIM_ISSUED).onlyWhen(claimIssued);
    }
}
