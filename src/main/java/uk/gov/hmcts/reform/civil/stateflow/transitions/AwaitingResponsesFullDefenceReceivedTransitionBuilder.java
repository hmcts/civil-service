package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ResponsePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AwaitingResponsesFullDefenceReceivedTransitionBuilder extends MidTransitionBuilder {

    public AwaitingResponsesFullDefenceReceivedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(ALL_RESPONSES_RECEIVED, transitions)
            .onlyWhen(ResponsePredicate.allResponsesReceived, transitions)

            .moveTo(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED, transitions)
            .onlyWhen(TakenOfflinePredicate.afterClaimDetailsNotified, transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline), transitions)

            .moveTo(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(DismissedPredicate.pastClaimDetailsNotificationDeadline, transitions);
    }

}
