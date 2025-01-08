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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineBySystem;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PendingClaimIssuedUnrepresentedDefendantOneVOneSpecTransitionBuilder extends MidTransitionBuilder {

    public PendingClaimIssuedUnrepresentedDefendantOneVOneSpecTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_ISSUED, transitions)
            .onlyWhen(claimIssued.and(pinInPostEnabledAndLiP), transitions)
            .moveTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT, transitions)
            .onlyWhen(takenOfflineBySystem.and(not(pinInPostEnabledAndLiP)), transitions);
    }

    public static final Predicate<CaseData> pinInPostEnabledAndLiP = caseData ->
        caseData.getRespondent1PinToPostLRspec() != null;
}
