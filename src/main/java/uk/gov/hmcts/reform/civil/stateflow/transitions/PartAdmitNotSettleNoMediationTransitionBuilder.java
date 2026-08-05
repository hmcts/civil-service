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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PartAdmitNotSettleNoMediationTransitionBuilder extends MidTransitionBuilder {

    public PartAdmitNotSettleNoMediationTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        addHearingReadinessTransitions(
            transitions,
            DismissedPredicate.pastHearingFeeDue,
            TakenOfflinePredicate.byStaff
        );
    }

}
