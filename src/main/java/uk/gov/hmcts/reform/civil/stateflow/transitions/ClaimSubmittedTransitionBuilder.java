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

    private static final Predicate<CaseData> TAKEN_OFFLINE_BY_STAFF_BEFORE_CLAIM_ISSUE =
        TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.beforeClaimIssue);

    @Autowired
    public ClaimSubmittedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_SUBMITTED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL, transitions)
            .onlyWhen(PaymentPredicate.successful.and(not(TAKEN_OFFLINE_BY_STAFF_BEFORE_CLAIM_ISSUE)), transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TAKEN_OFFLINE_BY_STAFF_BEFORE_CLAIM_ISSUE, transitions)

            .moveTo(CLAIM_ISSUED_PAYMENT_FAILED, transitions)
            .onlyWhen(PaymentPredicate.failed, transitions)

            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(LipPredicate.isLiPvLiPCase
                .and(TAKEN_OFFLINE_BY_STAFF_BEFORE_CLAIM_ISSUE.negate()), transitions)
            .set(this::setLipVLipCaseIssueFlags, transitions)

            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(LipPredicate.nocApplyForLiPClaimant, transitions)
            .set(flags -> setLipCaseFlags(flags, false, true), transitions)

            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(not(defendantNoCOnlineForCase())
                .and(LipPredicate.isLiPvLRCase.and(not(LipPredicate.nocSubmittedForLiPDefendant))
                .and(not(LipPredicate.nocSubmittedForLiPDefendantBeforeOffline))), transitions)
            .set(flags -> setLipCaseFlags(flags, true, false), transitions)

            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(defendantNoCOnlineForCase().and(LipPredicate.isLiPvLRCase), transitions)
            .set(this::setLipVlrNoCOnlineFlags, transitions)

            .moveTo(SPEC_DEFENDANT_NOC, transitions)
            .onlyWhen(
                not(defendantNoCOnlineForCase()).and(LipPredicate.nocSubmittedForLiPDefendantBeforeOffline),
                transitions
            )
            .set(flags -> setLipCaseFlags(flags, true, false), transitions);
    }

    private void setLipVLipCaseIssueFlags(CaseData caseData, Map<String, Boolean> flags) {
        setLipCaseFlags(flags, true, true);
        setClaimIssueBilingualFlag(caseData, flags);
        setClaimIssueHelpWithFeesFlag(caseData, flags);
    }

    private void setLipVlrNoCOnlineFlags(CaseData caseData, Map<String, Boolean> flags) {
        setLipCaseFlags(flags, true, false);
        setClaimIssueBilingualFlag(caseData, flags);
    }

    private void setClaimIssueBilingualFlag(CaseData caseData, Map<String, Boolean> flags) {
        if (LanguagePredicate.claimantIsBilingual.test(caseData)) {
            flags.put(FlowFlag.CLAIM_ISSUE_BILINGUAL.name(), true);
        }
    }

    private void setClaimIssueHelpWithFeesFlag(CaseData caseData, Map<String, Boolean> flags) {
        if (LipPredicate.isHelpWithFees.test(caseData)) {
            flags.put(FlowFlag.CLAIM_ISSUE_HWF.name(), true);
        }
    }

    private void setLipCaseFlags(Map<String, Boolean> flags, boolean lipCase, boolean unrepresentedDefendantOne) {
        flags.putAll(
            Map.of(
                FlowFlag.LIP_CASE.name(), lipCase,
                FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), unrepresentedDefendantOne
            )
        );
    }
}
