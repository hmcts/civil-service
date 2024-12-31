package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseDataParent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isLipCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.nocSubmittedForLiPDefendant;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.nocSubmittedForLiPDefendantBeforeOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
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
        this.moveTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL, transitions).onlyWhen(paymentSuccessful, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaffBeforeClaimIssued, transitions)
            .moveTo(CLAIM_ISSUED_PAYMENT_FAILED, transitions).onlyWhen(paymentFailed, transitions)
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions).onlyWhen(
                isLipCase,
                transitions
            )
            .set(
                (c, flags) -> {
                    if (featureToggleService.isPinInPostEnabled()) {
                        flags.put(FlowFlag.PIP_ENABLED.name(), true);
                    }
                    if (claimIssueBilingual.test(c)) {
                        flags.put(FlowFlag.CLAIM_ISSUE_BILINGUAL.name(), true);
                    }
                    if (claimIssueHwF.test(c)) {
                        flags.put(FlowFlag.CLAIM_ISSUE_HWF.name(), true);
                    }
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                    flags.put(FlowFlag.LIP_CASE.name(), true);
                }, transitions
            )
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions).onlyWhen(
                nocSubmittedForLiPApplicant,
                transitions
            )
            .set(
                flags -> flags.putAll(
                    Map.of(
                        FlowFlag.LIP_CASE.name(), false,
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true
                    )), transitions
            )
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(
                not(isDefendantNoCOnlineForCase).and(isLiPvLRCase.and(not(nocSubmittedForLiPDefendant))
                        .and(not(nocSubmittedForLiPDefendantBeforeOffline))),
                transitions
            )
            .set(
                flags -> flags.putAll(
                    Map.of(
                        FlowFlag.LIP_CASE.name(), true,
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                    )), transitions
            )
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, transitions)
            .onlyWhen(
                isDefendantNoCOnlineForCase.and(isLiPvLRCase), transitions
            )
            .set(
                flags -> flags.putAll(
                    Map.of(
                        FlowFlag.LIP_CASE.name(), true,
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                    )), transitions
            )
            .moveTo(SPEC_DEFENDANT_NOC, transitions).onlyWhen(not(isDefendantNoCOnlineForCase).and(
                nocSubmittedForLiPDefendantBeforeOffline), transitions)
            .set(
                flags -> flags.putAll(
                    Map.of(
                        FlowFlag.LIP_CASE.name(), true,
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                    )), transitions
        );
    }

    public static final Predicate<CaseData> paymentFailed = caseData ->
        !caseData.isApplicantNotRepresented()
            && ((caseData.getPaymentDetails() != null && caseData.getPaymentDetails().getStatus() == FAILED)
            || (caseData.getClaimIssuedPaymentDetails() != null && caseData.getClaimIssuedPaymentDetails().getStatus() == FAILED));

    public static final Predicate<CaseData> claimIssueBilingual = CaseDataParent::isClaimantBilingual;

    public static final Predicate<CaseData> claimIssueHwF = CaseData::isHelpWithFees;

    public static final Predicate<CaseData> takenOfflineByStaffBeforeClaimIssued =
        ClaimSubmittedTransitionBuilder::getPredicateTakenOfflineByStaffBeforeClaimIssue;

    public static boolean getPredicateTakenOfflineByStaffBeforeClaimIssue(CaseData caseData) {
        // In case of SPEC and UNSPEC claim ClaimNotificationDeadline will be set when the case is issued
        return caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimNotificationDeadline() == null
            && caseData.getClaimNotificationDate() == null
            && caseData.getSubmittedDate() != null;
    }

    public static final Predicate<CaseData> nocSubmittedForLiPApplicant = CaseData::nocApplyForLiPClaimant;

    public static final Predicate<CaseData> isLiPvLRCase = CaseData::isLipvLROneVOne;

    public final Predicate<CaseData> isDefendantNoCOnlineForCase = featureToggleService::isDefendantNoCOnlineForCase;
}
