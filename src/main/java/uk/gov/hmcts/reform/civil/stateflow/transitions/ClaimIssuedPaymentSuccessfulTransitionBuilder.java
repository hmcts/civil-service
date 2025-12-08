package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimPredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;
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
        this.moveTo(PENDING_CLAIM_ISSUED, transitions).onlyWhen(ClaimPredicate.pendingIssued, transitions)
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            // 2. Def1 unrepresented, Def2 registered
            // 3. Def1 registered, Def 2 unrepresented
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT, transitions)
            .onlyWhen(ClaimPredicate.pendingIssuedUnrepresented, transitions)
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(oneVsOneCase.and(ClaimPredicate.issuedRespondent1Unrepresented).and(ClaimPredicate.isSpec), transitions)
            .set(flags -> flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true), transitions)
            // Unregistered
            // 1. Both def1 and def2 unregistered
            // 2. Def1 unregistered, Def2 registered
            // 3. Def1 registered, Def 2 unregistered
            .moveTo(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT, transitions).onlyWhen(
                ((ClaimPredicate.issuedRespondent1OrgNotRegistered.and(ClaimPredicate.issuedRespondent1Unrepresented.negate()))
                    .and(ClaimPredicate.issuedRespondent2OrgNotRegistered.and(ClaimPredicate.issuedRespondent2Unrepresented.negate())))
                    .or((ClaimPredicate.issuedRespondent1OrgNotRegistered.and(ClaimPredicate.issuedRespondent1Unrepresented.negate()))
                        .and(ClaimPredicate.issuedRespondent2OrgNotRegistered.negate().and(ClaimPredicate.issuedRespondent2Unrepresented.negate())))
                    .or((ClaimPredicate.issuedRespondent1OrgNotRegistered.negate().and(ClaimPredicate.issuedRespondent1Unrepresented.negate()))
                        .and(ClaimPredicate.issuedRespondent2OrgNotRegistered.and(ClaimPredicate.issuedRespondent2Unrepresented.negate()))
                        .and(ClaimPredicate.sameRepresentationBoth.negate())
                    ), transitions
            )
            // Unrepresented and Unregistered
            // 1. Def1 unrepresented, Def2 unregistered
            // 2. Def1 unregistered, Def 2 unrepresented
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT, transitions).onlyWhen(
                (ClaimPredicate.issuedRespondent1Unrepresented.and(ClaimPredicate.issuedRespondent2OrgNotRegistered.and(ClaimPredicate.issuedRespondent2Unrepresented.negate())))
                    .or(ClaimPredicate.issuedRespondent1OrgNotRegistered.and(ClaimPredicate.issuedRespondent1Unrepresented.negate())
                        .and(ClaimPredicate.issuedRespondent2Unrepresented)), transitions);
    }

    public static final Predicate<CaseData> oneVsOneCase = ClaimIssuedPaymentSuccessfulTransitionBuilder::getPredicateFor1v1Case;

    private static boolean getPredicateFor1v1Case(CaseData caseData) {
        return ONE_V_ONE.equals(getMultiPartyScenario(caseData));
    }

    public static final Predicate<CaseData> multipartyCase = ClaimIssuedPaymentSuccessfulTransitionBuilder::getPredicateForMultipartyCase;

    private static boolean getPredicateForMultipartyCase(CaseData caseData) {
        return isMultiPartyScenario(caseData);
    }
}
