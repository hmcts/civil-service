package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.nocSubmittedForLiPDefendant;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SpecDefendantNocTransitionBuilder extends MidTransitionBuilder {

    public SpecDefendantNocTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.SPEC_DEFENDANT_NOC, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(TAKEN_OFFLINE_SPEC_DEFENDANT_NOC, transitions).onlyWhen(
            not(isDefendantNoCOnlineForCase).and(nocSubmittedForLiPDefendant),
            transitions
        );
    }

    public final Predicate<CaseData> isDefendantNoCOnlineForCase = featureToggleService::isDefendantNoCOnlineForCase;
}
