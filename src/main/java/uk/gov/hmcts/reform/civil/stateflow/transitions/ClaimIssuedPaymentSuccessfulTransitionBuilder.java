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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
            .onlyWhen(oneVsOneCase.and(ClaimPredicate.issuedRespondentUnrepresented).and(ClaimPredicate.isSpec), transitions)
            .set(flags -> flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true), transitions)
            // Unregistered
            // 1. Both def1 and def2 unregistered
            // 2. Def1 unregistered, Def2 registered
            // 3. Def1 registered, Def 2 unregistered
            .moveTo(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT, transitions).onlyWhen(
                ((respondent1OrgNotRegistered.and(ClaimPredicate.issuedRespondentUnrepresented.negate()))
                    .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or((respondent1OrgNotRegistered.and(ClaimPredicate.issuedRespondentUnrepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                    .or((respondent1OrgNotRegistered.negate().and(ClaimPredicate.issuedRespondentUnrepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate()))
                        .and(bothDefSameLegalRep.negate())
                    ), transitions
            )
            // Unrepresented and Unregistered
            // 1. Def1 unrepresented, Def2 unregistered
            // 2. Def1 unregistered, Def 2 unrepresented
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT, transitions).onlyWhen(
                (ClaimPredicate.issuedRespondentUnrepresented.and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or(respondent1OrgNotRegistered.and(ClaimPredicate.issuedRespondentUnrepresented.negate())
                        .and(respondent2NotRepresented)), transitions);
    }

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
