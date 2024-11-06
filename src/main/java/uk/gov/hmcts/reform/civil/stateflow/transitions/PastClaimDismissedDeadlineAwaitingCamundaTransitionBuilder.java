package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PastClaimDismissedDeadlineAwaitingCamundaTransitionBuilder extends MidTransitionBuilder {

    public PastClaimDismissedDeadlineAwaitingCamundaTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE, transitions).onlyWhen(claimDismissedByCamunda, transitions);
    }
}
