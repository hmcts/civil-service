package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.function.Predicate;

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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;

@Component
public class ClaimIssuedPaymentSuccessfulTransitionBuilder extends MidTransitionBuilder {

    @Autowired
    public ClaimIssuedPaymentSuccessfulTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL, featureToggleService);

    }

    @Override
    void setUpTransitions() {
        this.moveTo(PENDING_CLAIM_ISSUED).onlyWhen(pendingClaimIssued)
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            // 2. Def1 unrepresented, Def2 registered
            // 3. Def1 registered, Def 2 unrepresented
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT)
            .onlyWhen(pendingClaimIssuedUnrepresentedDefendentPredicate())
            .set(flags -> {
                if (featureToggleService.isPinInPostEnabled()) {
                    flags.put(FlowFlag.PIP_ENABLED.name(), true);
                }
            })
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC)
            .onlyWhen(oneVsOneCase.and(respondent1NotRepresented).and(specClaim))
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
            .moveTo(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT).onlyWhen(
                ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                    .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                    .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate()))
                        .and(bothDefSameLegalRep.negate())
                    )
            )
            // Unrepresented and Unregistered
            // 1. Def1 unrepresented, Def2 unregistered
            // 2. Def1 unregistered, Def 2 unrepresented
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT).onlyWhen(
                (respondent1NotRepresented.and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or(respondent1OrgNotRegistered.and(respondent1NotRepresented.negate())
                        .and(respondent2NotRepresented)));
    }

    @NotNull
    public static Predicate<CaseData> pendingClaimIssuedUnrepresentedDefendentPredicate() {
        return (respondent1NotRepresented.and(respondent2NotRepresented))
            .or(respondent1NotRepresented.and(respondent2OrgNotRegistered.negate()))
            .or(respondent1OrgNotRegistered.negate().and(respondent2NotRepresented))
            .and(not(specClaim))
            .or(multipartyCase.and(respondent1NotRepresented.and(respondent2NotRepresented)
                    .or(respondent1NotRepresented.and(respondent2OrgNotRegistered.negate()))
                    .or(respondent1OrgNotRegistered.negate().and(respondent2NotRepresented)))
                .and(specClaim));
    }

}
