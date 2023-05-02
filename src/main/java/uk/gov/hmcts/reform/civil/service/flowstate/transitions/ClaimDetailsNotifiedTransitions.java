package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

public class ClaimDetailsNotifiedTransitions implements StateFlowEngineTransitions {
    @Override
    public State<FlowState.Main> defineTransitions(State<FlowState.Main> previousState) {
        return previousState            .state(CLAIM_DETAILS_NOTIFIED)
            .transitionTo(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION)
            .onlyIf(respondentTimeExtension.and(not(notificationAcknowledged)))
            //Acknowledging Claim First
            .transitionTo(NOTIFICATION_ACKNOWLEDGED).onlyIf(notificationAcknowledged)
            //Direct Response, without Acknowledging
            .transitionTo(ALL_RESPONSES_RECEIVED)
            .onlyIf(allResponsesReceived.and(not(notificationAcknowledged)).and(not(respondentTimeExtension)))
            .transitionTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
            .onlyIf(awaitingResponsesFullDefenceReceived
                        .and(not(notificationAcknowledged)).and(not(respondentTimeExtension))
                        .and(not(caseDismissedAfterDetailNotified)))
            .transitionTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
            .onlyIf(awaitingResponsesNonFullDefenceReceived
                        .and(not(notificationAcknowledged)).and(not(respondentTimeExtension)))
            .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimDetailsNotified)
            .transitionTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
            .onlyIf(caseDismissedAfterDetailNotified)
            .state(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION)
            .transitionTo(NOTIFICATION_ACKNOWLEDGED).onlyIf(notificationAcknowledged)
            .transitionTo(ALL_RESPONSES_RECEIVED).onlyIf((respondentTimeExtension).and(allResponsesReceived))
            .transitionTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
            .onlyIf((awaitingResponsesFullDefenceReceived).and(respondentTimeExtension)
                        .and(not(caseDismissedAfterDetailNotifiedExtension)))
            .transitionTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
            .onlyIf((awaitingResponsesNonFullDefenceReceived).and(respondentTimeExtension))
            .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimDetailsNotifiedExtension)
            .transitionTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
            .onlyIf(caseDismissedAfterDetailNotifiedExtension);
    }
}
