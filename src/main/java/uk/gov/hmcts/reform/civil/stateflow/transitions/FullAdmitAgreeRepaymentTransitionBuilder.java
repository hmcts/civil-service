package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isRespondentSignSettlementAgreement;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SIGN_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FullAdmitAgreeRepaymentTransitionBuilder extends MidTransitionBuilder {

    public FullAdmitAgreeRepaymentTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_ADMIT_AGREE_REPAYMENT, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(SIGN_SETTLEMENT_AGREEMENT, transitions).onlyWhen(isRespondentSignSettlementAgreement, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaff, transitions);
    }
}
