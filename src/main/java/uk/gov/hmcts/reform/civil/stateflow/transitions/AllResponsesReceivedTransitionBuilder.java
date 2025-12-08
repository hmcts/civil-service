package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DivergencePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ResponsePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AllResponsesReceivedTransitionBuilder extends MidTransitionBuilder {

    public AllResponsesReceivedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.ALL_RESPONSES_RECEIVED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(FULL_DEFENCE, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE), transitions)
            .moveTo(FULL_DEFENCE, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE), transitions)
            .moveTo(FULL_ADMISSION, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseType.FULL_ADMISSION)
                          .and(not(DivergencePredicate.divergentRespondGoOffline)), transitions)
            .moveTo(PART_ADMISSION, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseType.PART_ADMISSION)
                          .and(not(DivergencePredicate.divergentRespondGoOffline)), transitions)
            .moveTo(COUNTER_CLAIM, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseType.COUNTER_CLAIM)
                          .and(not(DivergencePredicate.divergentRespondGoOffline)), transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions)
            .onlyWhen(DivergencePredicate.divergentRespondGoOffline, transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions)
            .onlyWhen(DivergencePredicate.divergentRespondGoOfflineSpec, transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_ADMISSION), transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.PART_ADMISSION), transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.COUNTER_CLAIM), transitions)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, transitions)
            .onlyWhen(DivergencePredicate.divergentRespondWithDQAndGoOffline, transitions)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, transitions)
            .onlyWhen(DivergencePredicate.divergentRespondWithDQAndGoOfflineSpec, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff
                          .and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension), transitions)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(DismissedPredicate.afterClaimDetailNotified, transitions);
    }

}
