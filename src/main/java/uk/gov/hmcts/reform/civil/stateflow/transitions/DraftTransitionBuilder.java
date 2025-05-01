package uk.gov.hmcts.reform.civil.stateflow.transitions;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.GENERAL_APPLICATION_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.BILINGUAL_DOCS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.BULK_CLAIM_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.CASE_PROGRESSION_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DASHBOARD_SERVICE_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.JO_ONLINE_LIVE_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.IS_JO_LIVE_FEED_ACTIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DEFENDANT_NOC_ONLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.CLAIM_STATE_DURING_NOC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.WELSH_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseContainsLiP;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;

public abstract class DraftTransitionBuilder extends TransitionBuilder {

    public DraftTransitionBuilder(FlowState.Main initialState, FeatureToggleService featureToggleService) {
        super(initialState, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(claimSubmittedOneRespondentRepresentative.or(claimSubmitted1v1RespondentOneUnregistered), transitions)
            .set((c, flags) -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.ofEntries(
                    Map.entry(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), true),
                    Map.entry(GENERAL_APPLICATION_ENABLED.name(), switchTheGAFlagIfLipExists(c)),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled()),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isGaForWelshEnabled()),
                    Map.entry(BILINGUAL_DOCS.name(), lipPartySpecifiedBilingualDocs(c))
                )), transitions)
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(claimSubmittedTwoRegisteredRespondentRepresentatives
                .or(claimSubmittedTwoRespondentRepresentativesOneUnregistered)
                .or(claimSubmittedBothUnregisteredSolicitors), transitions)
            .set((c, flags) -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.ofEntries(
                    Map.entry(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false),
                    Map.entry(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true),
                    Map.entry(GENERAL_APPLICATION_ENABLED.name(), switchTheGAFlagIfLipExists(c)),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled()),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isGaForWelshEnabled()),
                    Map.entry(BILINGUAL_DOCS.name(), lipPartySpecifiedBilingualDocs(c))
                )), transitions)
            // Only one unrepresented defendant
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(claimSubmittedOneUnrepresentedDefendantOnly, transitions)
            .set((c, flags) -> flags.putAll(
                Map.ofEntries(
                    Map.entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    Map.entry(GENERAL_APPLICATION_ENABLED.name(), switchTheGAFlagIfLipExists(c)),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled()),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isGaForWelshEnabled()),
                    Map.entry(BILINGUAL_DOCS.name(), lipPartySpecifiedBilingualDocs(c))
                )), transitions)
            // Unrepresented defendant 1
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(claimSubmittedRespondent1Unrepresented
                .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())
                .and(claimSubmittedRespondent2Unrepresented.negate()), transitions)
            .set((c, flags) -> flags.putAll(
                Map.ofEntries(
                    Map.entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    Map.entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false),
                    Map.entry(GENERAL_APPLICATION_ENABLED.name(), switchTheGAFlagIfLipExists(c)),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled()),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isGaForWelshEnabled()),
                    Map.entry(BILINGUAL_DOCS.name(), lipPartySpecifiedBilingualDocs(c))
                )), transitions)
            // Unrepresented defendant 2
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(claimSubmittedRespondent2Unrepresented
                .and(claimSubmittedRespondent1Unrepresented.negate()), transitions)
            .set((c, flags) -> flags.putAll(
                Map.ofEntries(
                    Map.entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false),
                    Map.entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    Map.entry(GENERAL_APPLICATION_ENABLED.name(), switchTheGAFlagIfLipExists(c)),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled()),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isGaForWelshEnabled()),
                    Map.entry(BILINGUAL_DOCS.name(), lipPartySpecifiedBilingualDocs(c))
                )), transitions)
            // Unrepresented defendants
            .moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(claimSubmittedRespondent1Unrepresented.and(
                claimSubmittedRespondent2Unrepresented), transitions)
            .set((c, flags) -> flags.putAll(
                Map.ofEntries(
                    Map.entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    Map.entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    Map.entry(GENERAL_APPLICATION_ENABLED.name(), switchTheGAFlagIfLipExists(c)),
                    Map.entry(DASHBOARD_SERVICE_ENABLED.name(), isDashBoardEnabledForCase(c)),
                    Map.entry(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled()),
                    Map.entry(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()),
                    Map.entry(JO_ONLINE_LIVE_ENABLED.name(), featureToggleService.isJudgmentOnlineLive()),
                    Map.entry(IS_JO_LIVE_FEED_ACTIVE.name(), featureToggleService.isJOLiveFeedActive()),
                    Map.entry(DEFENDANT_NOC_ONLINE.name(), featureToggleService.isDefendantNoCOnlineForCase(c)),
                    Map.entry(CLAIM_STATE_DURING_NOC.name(), getMainClaimCcdState(c)),
                    Map.entry(WELSH_ENABLED.name(), featureToggleService.isGaForWelshEnabled()),
                    Map.entry(BILINGUAL_DOCS.name(), lipPartySpecifiedBilingualDocs(c))
                )), transitions);
    }

    public static final Predicate<CaseData> claimSubmittedOneRespondentRepresentative = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getRespondent1Represented() != NO
            && (caseData.getAddRespondent2() == null
            || caseData.getAddRespondent2() == NO
            || (caseData.getAddRespondent2() == YES && caseData.getRespondent2SameLegalRepresentative() == YES));

    public static final Predicate<CaseData> claimSubmittedTwoRegisteredRespondentRepresentatives = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == YES
            && caseData.getRespondent2SameLegalRepresentative() == NO
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2Represented() == YES
            && caseData.getRespondent1OrgRegistered() == YES
            && caseData.getRespondent2OrgRegistered() == YES;

    public static final Predicate<CaseData> claimSubmittedTwoRespondentRepresentativesOneUnregistered = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == YES
            && caseData.getRespondent2SameLegalRepresentative() == NO
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2Represented() == YES
            && ((caseData.getRespondent1OrgRegistered() == YES && caseData.getRespondent2OrgRegistered() == NO)
            || (caseData.getRespondent2OrgRegistered() == YES && caseData.getRespondent1OrgRegistered() == NO));

    public static final Predicate<CaseData> claimSubmitted1v1RespondentOneUnregistered = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == NO
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent1OrgRegistered() == NO;

    public static final Predicate<CaseData> claimSubmittedOneUnrepresentedDefendantOnly = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getRespondent1Represented() == NO
            && caseData.getAddRespondent2() != YES;

    public static final Predicate<CaseData> claimSubmittedRespondent1Unrepresented = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getRespondent1Represented() == NO;

    public static final Predicate<CaseData> claimSubmittedRespondent2Unrepresented = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == YES
            && caseData.getRespondent2Represented() == NO;

    public static final Predicate<CaseData> claimSubmittedBothUnregisteredSolicitors = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getRespondent1OrgRegistered() == NO
            && caseData.getAddRespondent2() == YES
            && caseData.getRespondent2OrgRegistered() == NO
            && (caseData.getRespondent2SameLegalRepresentative() == NO
            || caseData.getRespondent2SameLegalRepresentative() == null);

    public boolean lipPartySpecifiedBilingualDocs(CaseData caseData) {
        return (caseData.isLipClaimantSpecifiedBilingualDocuments() || caseData.isLipDefendantSpecifiedBilingualDocuments());
    }

    public boolean isDashBoardEnabledForCase(CaseData caseData) {
        return featureToggleService.isDashboardEnabledForCase(caseData) && caseContainsLiP.test(caseData);
    }

    public boolean switchTheGAFlagIfLipExists(CaseData caseData) {
        if (caseData.isLipCase()) {
            return featureToggleService.isGaForLipsEnabled();
        }
        return featureToggleService.isGeneralApplicationsEnabled();
    }

    private Boolean getMainClaimCcdState(CaseData caseData) {
        return caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION;
    }

}
