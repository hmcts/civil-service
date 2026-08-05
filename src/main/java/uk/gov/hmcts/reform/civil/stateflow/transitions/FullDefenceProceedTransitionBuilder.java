package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FullDefenceProceedTransitionBuilder extends MidTransitionBuilder {

    private static final Predicate<CaseData> TAKEN_OFFLINE_BY_STAFF_AROUND_SDO =
        TakenOfflinePredicate.byStaff.and(
            TakenOfflinePredicate.beforeSdo
                .or(TakenOfflinePredicate.afterSdo)
                .or(TakenOfflinePredicate.afterSdoNotSuitable)
        );

    public FullDefenceProceedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_DEFENCE_PROCEED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        addHearingReadinessTransitions(
            transitions,
            DismissedPredicate.pastHearingFeeDue,
            TAKEN_OFFLINE_BY_STAFF_AROUND_SDO.and(not(DismissedPredicate.pastHearingFeeDue))
        );
    }

}
