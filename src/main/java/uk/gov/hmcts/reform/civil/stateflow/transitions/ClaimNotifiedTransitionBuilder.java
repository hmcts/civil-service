package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
public class ClaimNotifiedTransitionBuilder extends MidTransitionBuilder {
    public ClaimNotifiedTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_NOTIFIED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(CLAIM_DETAILS_NOTIFIED).onlyWhen(claimDetailsNotified)
            .moveTo(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED).onlyWhen(takenOfflineAfterClaimDetailsNotified)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaffAfterClaimNotified)
            .moveTo(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(pastClaimDetailsNotificationDeadline);
    }
}
