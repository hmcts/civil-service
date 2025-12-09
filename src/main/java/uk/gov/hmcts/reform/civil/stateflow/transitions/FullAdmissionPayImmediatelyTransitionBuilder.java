package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.PaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import java.util.List;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_JUDGMENT_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FullAdmissionPayImmediatelyTransitionBuilder extends MidTransitionBuilder {

    public FullAdmissionPayImmediatelyTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_ADMIT_PAY_IMMEDIATELY, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.and(not(LipPredicate.ccjRequestJudgmentByAdmission)), transitions)

            .moveTo(FULL_ADMIT_JUDGMENT_ADMISSION, transitions)
            .onlyWhen(LipPredicate.ccjRequestJudgmentByAdmission.and(PaymentPredicate.payImmediatelyPartAdmission), transitions);
    }

}
