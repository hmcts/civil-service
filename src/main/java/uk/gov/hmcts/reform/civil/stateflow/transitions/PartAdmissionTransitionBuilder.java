package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;

import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.agreedToMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.acceptRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeNotBeingTakenOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.rejectRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_SETTLE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PartAdmissionTransitionBuilder extends MidTransitionBuilder {

    public PartAdmissionTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PART_ADMISSION, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(IN_MEDIATION).onlyWhen(agreedToMediation.and(not(takenOfflineByStaff)))
            .moveTo(PART_ADMIT_NOT_SETTLED_NO_MEDIATION)
            .onlyWhen(isClaimantNotSettlePartAdmitClaim.and(not(agreedToMediation)).and(not(takenOfflineByStaff)))
            .set((c, flags) -> {
                flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
            })
            .moveTo(PART_ADMIT_PROCEED).onlyWhen(fullDefenceProceed)
            .moveTo(PART_ADMIT_NOT_PROCEED).onlyWhen(fullDefenceNotProceed)
            .moveTo(PART_ADMIT_PAY_IMMEDIATELY).onlyWhen(partAdmitPayImmediately)
            .moveTo(PART_ADMIT_AGREE_SETTLE).onlyWhen(agreePartAdmitSettle)
            .moveTo(PART_ADMIT_AGREE_REPAYMENT).onlyWhen(acceptRepaymentPlan)
            .set((c, flags) -> flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c)))
            .moveTo(PART_ADMIT_REJECT_REPAYMENT).onlyWhen(rejectRepaymentPlan)
            .set((c, flags) -> flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c)))
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaff)
            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(applicantOutOfTimeNotBeingTakenOffline);
    }

    public static final Predicate<CaseData> agreePartAdmitSettle = CaseData::isPartAdmitClaimSettled;

    public static final Predicate<CaseData> partAdmitPayImmediately = CaseData::isPartAdmitPayImmediatelyAccepted;

    public static final Predicate<CaseData> isClaimantNotSettlePartAdmitClaim = CaseData::isClaimantNotSettlePartAdmitClaim;
}
