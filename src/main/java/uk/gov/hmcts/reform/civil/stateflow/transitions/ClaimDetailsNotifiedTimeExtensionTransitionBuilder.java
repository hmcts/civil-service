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

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimDetailsNotifiedTimeExtensionTransitionBuilder extends MidTransitionBuilder {

    public ClaimDetailsNotifiedTimeExtensionTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(NOTIFICATION_ACKNOWLEDGED, transitions)
            .onlyWhen(ResponsePredicate.notificationAcknowledged, transitions)
            .moveTo(ALL_RESPONSES_RECEIVED, transitions)
            .onlyWhen(
                (ResponsePredicate.respondentTimeExtension)
                    .and(ResponsePredicate.allResponsesReceived), transitions
            )
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, transitions)
            .onlyWhen(
                (ResponsePredicate.awaitingResponsesFullDefenceReceived).and(ResponsePredicate.respondentTimeExtension)
                    .and(not(DismissedPredicate.afterClaimNotifiedExtension)), transitions
            )
            .moveTo(AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(
                (ResponsePredicate.awaitingResponsesFullAdmitReceived)
                    .and(ResponsePredicate.respondentTimeExtension), transitions
            )
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(
                (ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived)
                    .and(ResponsePredicate.respondentTimeExtension), transitions
            )
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedExtension), transitions)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(DismissedPredicate.afterClaimNotifiedExtension, transitions)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions)
            .onlyWhen(
                TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedExtension), transitions
            );
    }
}
