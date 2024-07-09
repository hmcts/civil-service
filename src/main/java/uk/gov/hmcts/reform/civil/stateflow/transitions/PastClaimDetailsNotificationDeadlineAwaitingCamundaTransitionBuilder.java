package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;

@Component
public class PastClaimDetailsNotificationDeadlineAwaitingCamundaTransitionBuilder extends MidTransitionBuilder {

    public PastClaimDetailsNotificationDeadlineAwaitingCamundaTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE).onlyWhen(claimDismissedByCamunda);
    }
}
