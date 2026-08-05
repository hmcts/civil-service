package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static java.util.function.Predicate.not;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MediationUnsuccessfulProceedTransitionBuilder extends MidTransitionBuilder {

    public MediationUnsuccessfulProceedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.MEDIATION_UNSUCCESSFUL_PROCEED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        addHearingReadinessTransitions(
            transitions,
            DismissedPredicate.pastHearingFeeDue.and(not(TakenOfflinePredicate.byStaff)),
            TakenOfflinePredicate.byStaff
        );
    }

}
