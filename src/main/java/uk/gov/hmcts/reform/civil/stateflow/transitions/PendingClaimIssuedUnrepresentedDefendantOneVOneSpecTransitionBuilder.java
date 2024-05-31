package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pinInPostEnabledAndLiP;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineBySystem;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;

@Component
public class PendingClaimIssuedUnrepresentedDefendantOneVOneSpecTransitionBuilder extends MidTransitionBuilder {
    public PendingClaimIssuedUnrepresentedDefendantOneVOneSpecTransitionBuilder(
                                                                                FeatureToggleService featureToggleService) {
        super(FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, featureToggleService);
    }

    @Override
    void setUpTransitions() {
            this.moveTo(CLAIM_ISSUED)
            .onlyWhen(claimIssued.and(pinInPostEnabledAndLiP))
            .moveTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT)
            .onlyWhen(takenOfflineBySystem.and(not(pinInPostEnabledAndLiP)));
    }
}
