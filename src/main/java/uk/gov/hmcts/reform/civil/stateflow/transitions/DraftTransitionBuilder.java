package uk.gov.hmcts.reform.civil.stateflow.transitions;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.BULK_CLAIM_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.CLAIM_STATE_DURING_NOC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DASHBOARD_SERVICE_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DEFENDANT_NOC_ONLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.IS_JO_LIVE_FEED_ACTIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.JO_ONLINE_LIVE_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.ONE_RESPONDENT_REPRESENTATIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_TWO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.WELSH_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.JBA_ISSUED_BEFORE_NOC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.IS_CJES_SERVICE_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;

public abstract class DraftTransitionBuilder extends TransitionBuilder {

    public DraftTransitionBuilder(FlowState.Main initialState, FeatureToggleService featureToggleService) {
        super(initialState, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(ClaimPredicate.submittedOneRespondentRepresentative
                .or(ClaimPredicate.submitted1v1RespondentOneUnregistered), transitions)
            .set((c, flags) -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.ofEntries(
                    Map.entry(ONE_RESPONDENT_REPRESENTATIVE.name(), true),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isWelshEnabledForMainCase()),
                    Map.entry(JBA_ISSUED_BEFORE_NOC.name(), isJudgmentByAdmissionIssuedForCase(c)),
                    Map.entry(IS_CJES_SERVICE_ENABLED.name(), featureToggleService.isCjesServiceAvailable())
                )), transitions)

            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(
                ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives
                    .or(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered)
                    .or(ClaimPredicate.submittedBothUnregisteredSolicitors), transitions
            )
            .set((c, flags) -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.ofEntries(
                    Map.entry(ONE_RESPONDENT_REPRESENTATIVE.name(), false),
                    Map.entry(TWO_RESPONDENT_REPRESENTATIVES.name(), true),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isWelshEnabledForMainCase()),
                    Map.entry(JBA_ISSUED_BEFORE_NOC.name(), isJudgmentByAdmissionIssuedForCase(c)),
                    Map.entry(IS_CJES_SERVICE_ENABLED.name(), featureToggleService.isCjesServiceAvailable())
                )), transitions)

            // Only one unrepresented defendant
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(ClaimPredicate.submittedOneUnrepresentedDefendantOnly, transitions)
            .set((c, flags) -> flags.putAll(
                Map.ofEntries(
                    Map.entry(UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isWelshEnabledForMainCase()),
                    Map.entry(JBA_ISSUED_BEFORE_NOC.name(), isJudgmentByAdmissionIssuedForCase(c)),
                    Map.entry(IS_CJES_SERVICE_ENABLED.name(), featureToggleService.isCjesServiceAvailable())
                )), transitions)

            // Unrepresented defendant 1
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(
                ClaimPredicate.submittedRespondent1Unrepresented
                    .and(ClaimPredicate.submittedOneUnrepresentedDefendantOnly.negate())
                    .and(ClaimPredicate.submittedRespondent2Unrepresented.negate()), transitions
            )
            .set((c, flags) -> flags.putAll(
                Map.ofEntries(
                    Map.entry(UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    Map.entry(UNREPRESENTED_DEFENDANT_TWO.name(), false),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isWelshEnabledForMainCase()),
                    Map.entry(JBA_ISSUED_BEFORE_NOC.name(), isJudgmentByAdmissionIssuedForCase(c)),
                    Map.entry(IS_CJES_SERVICE_ENABLED.name(), featureToggleService.isCjesServiceAvailable())
                )), transitions)

            // Unrepresented defendant 2
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(
                ClaimPredicate.submittedRespondent2Unrepresented
                    .and(ClaimPredicate.submittedRespondent1Unrepresented.negate()), transitions
            )
            .set((c, flags) -> flags.putAll(
                Map.ofEntries(
                    Map.entry(UNREPRESENTED_DEFENDANT_ONE.name(), false),
                    Map.entry(UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isWelshEnabledForMainCase()),
                    Map.entry(JBA_ISSUED_BEFORE_NOC.name(), isJudgmentByAdmissionIssuedForCase(c)),
                    Map.entry(IS_CJES_SERVICE_ENABLED.name(), featureToggleService.isCjesServiceAvailable())
                )), transitions)

            // Unrepresented defendants
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(ClaimPredicate.submittedRespondent1Unrepresented
                .and(ClaimPredicate.submittedRespondent2Unrepresented), transitions)
            .set((c, flags) -> flags.putAll(
                Map.ofEntries(
                    Map.entry(UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    Map.entry(UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isWelshEnabledForMainCase()),
                    Map.entry(JBA_ISSUED_BEFORE_NOC.name(), isJudgmentByAdmissionIssuedForCase(c)),
                    Map.entry(IS_CJES_SERVICE_ENABLED.name(), featureToggleService.isCjesServiceAvailable())
                )), transitions);
    }

    public boolean isDashBoardEnabledForCase(CaseData caseData) {
        return featureToggleService.isDashboardEnabledForCase(caseData) && LipPredicate.caseContainsLiP.test(caseData);
    }

    private Boolean getMainClaimCcdState(CaseData caseData) {
        return caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION;
    }

    public boolean isJudgmentByAdmissionIssuedForCase(CaseData caseData) {
        return LipPredicate.caseContainsLiP.test(caseData)
            && featureToggleService.isJudgmentOnlineLive()
            && caseData.getActiveJudgment() != null
            && JudgmentType.JUDGMENT_BY_ADMISSION.equals(caseData.getActiveJudgment().getType())
            && caseData.getCcdState() == CaseState.All_FINAL_ORDERS_ISSUED;
    }
}
