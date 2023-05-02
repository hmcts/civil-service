package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionTo;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.bothDefSameLegalRep;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.multipartyCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.oneVsOneCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pendingClaimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;

@Component
@RequiredArgsConstructor
public class ClaimIssuedPaymentSuccessfulTransitions implements StateFlowEngineTransitions {

    private final FeatureToggleService featureToggleService;

    @Override
    public State<FlowState.Main> defineTransitions(State<FlowState.Main> previousState) {
        TransitionTo<FlowState.Main> builder = previousState.state(CLAIM_ISSUED_PAYMENT_SUCCESSFUL);
        return builder.transitionTo(PENDING_CLAIM_ISSUED)
                .onlyIf(pendingClaimIssued)
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            // 2. Def1 unrepresented, Def2 registered
            // 3. Def1 registered, Def 2 unrepresented
            .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT)
                .onlyIf((respondent1NotRepresented.and(respondent2NotRepresented))
                    .or(respondent1NotRepresented.and(respondent2OrgNotRegistered.negate()))
                    .or(respondent1OrgNotRegistered.negate().and(respondent2NotRepresented))
                    .and(not(specClaim)))
            .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC)
                .onlyIf(oneVsOneCase.and(respondent1NotRepresented).and(specClaim))
                .set(flags -> {
                    if (featureToggleService.isPinInPostEnabled()) {
                        flags.put(FlowFlag.PIP_ENABLED.name(), true);
                    }
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                })
            .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT)
                .onlyIf(multipartyCase.and(respondent1NotRepresented.and(respondent2NotRepresented)
                    .or(respondent1NotRepresented.and(respondent2OrgNotRegistered.negate()))
                    .or(respondent1OrgNotRegistered.negate().and(respondent2NotRepresented)))
                    .and(specClaim))
                .set(flags -> {
                    if (featureToggleService.isPinInPostEnabled()) {
                        flags.put(FlowFlag.PIP_ENABLED.name(), true);
                    }
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                })
            // Unregistered
            // 1. Both def1 and def2 unregistered
            // 2. Def1 unregistered, Def2 registered
            // 3. Def1 registered, Def 2 unregistered
            .transitionTo(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT)
                .onlyIf(((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate()))
                    )
                    .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate()))
                    )
                    .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate()))
                        .and(bothDefSameLegalRep.negate())
                    )
            )
            // Unrepresented and Unregistered
            // 1. Def1 unrepresented, Def2 unregistered
            // 2. Def1 unregistered, Def 2 unrepresented
            .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT).onlyIf(
                (respondent1NotRepresented.and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or(respondent1OrgNotRegistered.and(respondent1NotRepresented.negate())
                            .and(respondent2NotRepresented)));

    }
}
