package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;

@Component
public class ClaimIssuedPaymentFailedTransitionBuilder extends TransitionBuilder {

    @Autowired
    public ClaimIssuedPaymentFailedTransitionBuilder(CaseDetailsConverter caseDetailsConverter,
                                                     FeatureToggleService featureToggleService) {
        super(caseDetailsConverter, featureToggleService, FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED);
    }

    @Override
    public List<Transition> buildTransitions() {
        return this.moveTo(FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyWhen(paymentSuccessful).buildTransitions();

    }
}
