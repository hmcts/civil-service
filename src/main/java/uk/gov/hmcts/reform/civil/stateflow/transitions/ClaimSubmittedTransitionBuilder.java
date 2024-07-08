package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseDataParent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.Map;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isLipCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
public class ClaimSubmittedTransitionBuilder extends MidTransitionBuilder {

    @Autowired
    public ClaimSubmittedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_SUBMITTED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyWhen(paymentSuccessful)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaffBeforeClaimIssued)
            .moveTo(CLAIM_ISSUED_PAYMENT_FAILED).onlyWhen(paymentFailed)
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC).onlyWhen(isLipCase)
            .set((c, flags) -> {
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
            })
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC).onlyWhen(nocSubmittedForLiPApplicant)
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.LIP_CASE.name(), false,
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true
                )))
            .moveTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC).onlyWhen(isLiPvLRCase)
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.LIP_CASE.name(), true,
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                )));
    }

    public static final Predicate<CaseData> paymentFailed = caseData ->
        !caseData.isApplicantNotRepresented()
            && ((caseData.getPaymentDetails() != null && caseData.getPaymentDetails().getStatus() == FAILED)
            || (caseData.getClaimIssuedPaymentDetails() != null && caseData.getClaimIssuedPaymentDetails().getStatus() == FAILED));

    public static final Predicate<CaseData> claimIssueBilingual = CaseDataParent::isClaimantBilingual;

    public static final Predicate<CaseData> claimIssueHwF = CaseData::isHelpWithFees;

    public static final Predicate<CaseData> takenOfflineByStaffBeforeClaimIssued = ClaimSubmittedTransitionBuilder::getPredicateTakenOfflineByStaffBeforeClaimIssue;

    public static boolean getPredicateTakenOfflineByStaffBeforeClaimIssue(CaseData caseData) {
        // In case of SPEC and UNSPEC claim ClaimNotificationDeadline will be set when the case is issued
        return caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimNotificationDeadline() == null
            && caseData.getClaimNotificationDate() == null
            && caseData.getSubmittedDate() != null;
    }

    public static final Predicate<CaseData> nocSubmittedForLiPApplicant = CaseData::nocApplyForLiPClaimant;

    public static final Predicate<CaseData> isLiPvLRCase = CaseData::isLipvLROneVOne;
}
