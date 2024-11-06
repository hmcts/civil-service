package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimIssuedPaymentSuccessfulTransitionBuilder extends MidTransitionBuilder {

    @Autowired
    public ClaimIssuedPaymentSuccessfulTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(PENDING_CLAIM_ISSUED, transitions).onlyWhen(pendingClaimIssued, transitions)
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            // 2. Def1 unrepresented, Def2 registered
            // 3. Def1 registered, Def 2 unrepresented
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT, transitions)
            .onlyWhen(pendingClaimIssuedUnrepresentedDefendentPredicate(), transitions)
            .set(flags -> {
                if (featureToggleService.isPinInPostEnabled()) {
                    flags.put(FlowFlag.PIP_ENABLED.name(), true);
                }
            }, transitions)
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(oneVsOneCase.and(respondent1NotRepresented).and(specClaim), transitions)
            .set(flags -> {
                if (featureToggleService.isPinInPostEnabled()) {
                    flags.put(FlowFlag.PIP_ENABLED.name(), true);
                }
                flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
            }, transitions)
            // Unregistered
            // 1. Both def1 and def2 unregistered
            // 2. Def1 unregistered, Def2 registered
            // 3. Def1 registered, Def 2 unregistered
            .moveTo(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT, transitions).onlyWhen(
                ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                    .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                    .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate()))
                        .and(bothDefSameLegalRep.negate())
                    ), transitions
            )
            // Unrepresented and Unregistered
            // 1. Def1 unrepresented, Def2 unregistered
            // 2. Def1 unregistered, Def 2 unrepresented
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT, transitions).onlyWhen(
                (respondent1NotRepresented.and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or(respondent1OrgNotRegistered.and(respondent1NotRepresented.negate())
                        .and(respondent2NotRepresented)), transitions);
    }

    public static final Predicate<CaseData> pendingClaimIssued = caseData ->
        caseData.getIssueDate() != null
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent1OrgRegistered() == YES
            && (caseData.getRespondent2() == null
            || (caseData.getRespondent2Represented() == YES
            && (caseData.getRespondent2OrgRegistered() == YES
            || caseData.getRespondent2SameLegalRepresentative() == YES)));

    @NotNull
    public static Predicate<CaseData> pendingClaimIssuedUnrepresentedDefendentPredicate() {
        return respondent1NotRepresented.and(respondent2NotRepresented)
            .or(respondent1NotRepresented.and(not(respondent2OrgNotRegistered)))
            .or(not(respondent1OrgNotRegistered).and(respondent2NotRepresented))
            .and(not(specClaim))
            .or(multipartyCase.and(respondent1NotRepresented.and(respondent2NotRepresented)
                    .or(respondent1NotRepresented.and(not(respondent2OrgNotRegistered)))
                    .or(not(respondent1OrgNotRegistered).and(respondent2NotRepresented)))
                .and(specClaim));
    }

    public static final Predicate<CaseData> respondent1NotRepresented = caseData ->
        caseData.getIssueDate() != null && caseData.getRespondent1Represented() == NO;

    public static final Predicate<CaseData> respondent1OrgNotRegistered = caseData ->
        caseData.getIssueDate() != null
            && caseData.getRespondent1OrgRegistered() == NO
            && caseData.getRespondent1Represented() == YES;

    public static final Predicate<CaseData> respondent2NotRepresented = caseData ->
        caseData.getIssueDate() != null && caseData.getRespondent2Represented() == NO;

    public static final Predicate<CaseData> respondent2OrgNotRegistered = caseData ->
        caseData.getIssueDate() != null
            && caseData.getRespondent2Represented() == YES
            && caseData.getRespondent2OrgRegistered() != YES;

    public static final Predicate<CaseData> bothDefSameLegalRep = caseData ->
        caseData.getRespondent2SameLegalRepresentative() == YES;

    public static final Predicate<CaseData> oneVsOneCase = ClaimIssuedPaymentSuccessfulTransitionBuilder::getPredicateFor1v1Case;

    private static boolean getPredicateFor1v1Case(CaseData caseData) {
        return ONE_V_ONE.equals(getMultiPartyScenario(caseData));
    }

    public static final Predicate<CaseData> multipartyCase = ClaimIssuedPaymentSuccessfulTransitionBuilder::getPredicateForMultipartyCase;

    private static boolean getPredicateForMultipartyCase(CaseData caseData) {
        return isMultiPartyScenario(caseData);
    }
}
