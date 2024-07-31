package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineBySystem;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PendingClaimIssuedUnregisteredDefendantTransitionBuilder extends MidTransitionBuilder {

    public PendingClaimIssuedUnregisteredDefendantTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT).onlyWhen(takenOfflineBySystem);
    }
}
