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

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isDefendantNoCOnlineForCaseAfterJBA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PartAdmissionPayImmediatelyTransitionBuilder extends MidTransitionBuilder {

    public PartAdmissionPayImmediatelyTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PART_ADMIT_PAY_IMMEDIATELY, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaff, transitions)
            .moveTo(TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA, transitions)
            .onlyWhen(isDefendantNoCOnlineForCase.and(isDefendantNoCOnlineForCaseAfterJBA), transitions);
    }

    public final Predicate<CaseData> isDefendantNoCOnlineForCase = featureToggleService::isDefendantNoCOnlineForCase;
}
