package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
public class ClaimDismissedPastClaimNotificationDeadlineTransitionBuilder extends MidTransitionBuilder {
    public ClaimDismissedPastClaimNotificationDeadlineTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaff);
    }
}
