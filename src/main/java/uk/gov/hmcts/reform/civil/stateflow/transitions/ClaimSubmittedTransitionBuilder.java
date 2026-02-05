package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.PaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DEFENDANT_NOC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimSubmittedTransitionBuilder extends MidTransitionBuilder {

    @Autowired
    public ClaimSubmittedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_SUBMITTED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL, transitions)
            .onlyWhen(PaymentPredicate.successful, transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.beforeClaimIssue), transitions)

            .moveTo(CLAIM_ISSUED_PAYMENT_FAILED, transitions)
            .onlyWhen(PaymentPredicate.failed, transitions)

            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(LipPredicate.isLiPvLiPCase
                .and(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.beforeClaimIssue).negate()), transitions)
            .set(
                (c, flags) -> {
                    if (LanguagePredicate.claimantIsBilingual.test(c)) {
                        flags.put(FlowFlag.CLAIM_ISSUE_BILINGUAL.name(), true);
                    }
                    if (LipPredicate.isHelpWithFees.test(c)) {
                        flags.put(FlowFlag.CLAIM_ISSUE_HWF.name(), true);
                    }
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                    flags.put(FlowFlag.LIP_CASE.name(), true);
                }, transitions)

            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(LipPredicate.nocApplyForLiPClaimant, transitions)
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.LIP_CASE.name(), false,
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true
                )), transitions)

            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(not(isDefendantNoCOnlineForCase)
                .and(LipPredicate.isLiPvLRCase.and(not(LipPredicate.nocSubmittedForLiPDefendant))
                .and(not(LipPredicate.nocSubmittedForLiPDefendantBeforeOffline))), transitions)
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.LIP_CASE.name(), true,
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                )), transitions)

            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(isDefendantNoCOnlineForCase.and(LipPredicate.isLiPvLRCase), transitions)
            .set(
                (c, flags) -> {
                    flags.putAll(
                        Map.of(
                            FlowFlag.LIP_CASE.name(), true,
                            FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                        )
                    );
                    if (LanguagePredicate.claimantIsBilingual.test(c)) {
                        flags.put(FlowFlag.CLAIM_ISSUE_BILINGUAL.name(), true);
                    }
                }, transitions)

            .moveTo(SPEC_DEFENDANT_NOC, transitions).onlyWhen(not(isDefendantNoCOnlineForCase).and(
                LipPredicate.nocSubmittedForLiPDefendantBeforeOffline), transitions)
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.LIP_CASE.name(), true,
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                )), transitions);
    }

    public final Predicate<CaseData> isDefendantNoCOnlineForCase = featureToggleService::isDefendantNoCOnlineForCase;
}
