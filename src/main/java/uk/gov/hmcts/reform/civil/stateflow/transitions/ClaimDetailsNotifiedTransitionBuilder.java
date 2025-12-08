package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.HearingPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ResponsePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimDetailsNotifiedTransitionBuilder extends MidTransitionBuilder {

    public ClaimDetailsNotifiedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_DETAILS_NOTIFIED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION, transitions)
            .onlyWhen(ResponsePredicate.respondentTimeExtension
                .and(not(ResponsePredicate.notificationAcknowledged)).and(not(HearingPredicate.isInReadiness)), transitions)
            // Acknowledging Claim First
            .moveTo(NOTIFICATION_ACKNOWLEDGED, transitions)
            .onlyWhen(ResponsePredicate.notificationAcknowledged.and(not(HearingPredicate.isInReadiness)), transitions)
            // Direct Response, without Acknowledging
            .moveTo(ALL_RESPONSES_RECEIVED, transitions)
            .onlyWhen(ResponsePredicate.allResponsesReceived.and(not(ResponsePredicate.notificationAcknowledged)
                .and(not(ResponsePredicate.respondentTimeExtension)).and(not(HearingPredicate.isInReadiness))), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, transitions)
            .onlyWhen(ResponsePredicate.awaitingResponsesFullDefenceReceived
                .and(not(ResponsePredicate.notificationAcknowledged)).and(not(ResponsePredicate.respondentTimeExtension))
                .and(not(DismissedPredicate.afterClaimDetailNotified)), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(ResponsePredicate.awaitingResponsesFullAdmitReceived
                .and(not(ResponsePredicate.notificationAcknowledged)).and(not(ResponsePredicate.respondentTimeExtension)), transitions)
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived
                .and(not(ResponsePredicate.notificationAcknowledged)).and(not(ResponsePredicate.respondentTimeExtension)), transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension), transitions)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(DismissedPredicate.afterClaimDetailNotified.and(not(HearingPredicate.isInReadiness)), transitions)
            .moveTo(IN_HEARING_READINESS, transitions).onlyWhen(HearingPredicate.isInReadiness, transitions)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions)
            .onlyWhen(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension), transitions);
    }

}
