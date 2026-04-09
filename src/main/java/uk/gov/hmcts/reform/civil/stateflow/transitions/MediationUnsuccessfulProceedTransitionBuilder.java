package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.HearingPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MediationUnsuccessfulProceedTransitionBuilder extends MidTransitionBuilder {

    public MediationUnsuccessfulProceedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.MEDIATION_UNSUCCESSFUL_PROCEED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(IN_HEARING_READINESS, transitions)
            .onlyWhen(HearingPredicate.isInReadiness, transitions)

            .moveTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE, transitions)
            .onlyWhen(DismissedPredicate.pastHearingFeeDue.and(not(TakenOfflinePredicate.byStaff)), transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff, transitions)

            .moveTo(TAKEN_OFFLINE_AFTER_SDO, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.negate()
                .and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)), transitions)

            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.sdoNotDrawn), transitions);
    }

}
