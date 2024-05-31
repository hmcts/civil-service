package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;

import java.util.Map;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.agreedToMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.declinedMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isClaimantNotSettleFullDefenceClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isClaimantSettleTheClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isDefendantNotPaidFullDefenceClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isLipCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allAgreedToLrMediationSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.demageMultiClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isCarmApplicableLipCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.lipFullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterDefendantResponse;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
public class FullDefenceTransitionBuilder extends MidTransitionBuilder{
    
    public FullDefenceTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_DEFENCE, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(IN_MEDIATION).onlyWhen((agreedToMediation.and(allAgreedToLrMediationSpec.negate()))
                // for carm cases, fullDefenceProcced is tracked with lipFullDefenceProceed
                // and move to in mediation if applicant does not settle
                .or(lipFullDefenceProceed.and(isCarmApplicableLipCase).and(not(fullDefenceProceed))))
            .moveTo(FULL_DEFENCE_PROCEED)
            .onlyWhen(fullDefenceProceed.and(allAgreedToLrMediationSpec).and(agreedToMediation.negate()).and(declinedMediation.negate()))
            .set((c, flags) -> {
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), true);
                flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c));
            })
            .moveTo(FULL_DEFENCE_PROCEED)
            .onlyWhen(fullDefenceProceed.and(allAgreedToLrMediationSpec.negate().and(agreedToMediation.negate()))
                .or(declinedMediation).and(applicantOutOfTime.negate()).and(demageMultiClaim))
            .set((c, flags) -> {
                flags.put(FlowFlag.IS_MULTI_TRACK.name(), true);
                flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c));
            })
            .moveTo(FULL_DEFENCE_PROCEED)
            .onlyWhen(fullDefenceProceed.and(allAgreedToLrMediationSpec.negate().and(agreedToMediation.negate()))
                .or(declinedMediation).and(applicantOutOfTime.negate()).and(demageMultiClaim.negate()).and(isLipCase.negate()))
            .setDynamic(Map.of(FlowFlag.SDO_ENABLED.name(),
                JudicialReferralUtils::shouldMoveToJudicialReferral))
            .moveTo(FULL_DEFENCE_PROCEED)
            .onlyWhen((fullDefenceProceed.or(isClaimantNotSettleFullDefenceClaim).or(isDefendantNotPaidFullDefenceClaim))
                .and(not(agreedToMediation)).and(isCarmApplicableLipCase.negate()).and(isLipCase))
            .set((c, flags) -> {
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), false);
            })
            .moveTo(FULL_DEFENCE_PROCEED).onlyWhen(isClaimantSettleTheClaim.and(not(agreedToMediation)))
            .set((c, flags) -> {
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), true);
            })
            .moveTo(FULL_DEFENCE_NOT_PROCEED).onlyWhen(fullDefenceNotProceed)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaffAfterDefendantResponse)
            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(applicantOutOfTime);
    }
}
