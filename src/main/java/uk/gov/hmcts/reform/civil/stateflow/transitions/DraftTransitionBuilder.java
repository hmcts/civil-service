package uk.gov.hmcts.reform.civil.stateflow.transitions;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.Map;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.BULK_CLAIM_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DASHBOARD_SERVICE_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.GENERAL_APPLICATION_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;

public abstract class DraftTransitionBuilder extends TransitionBuilder {

    public DraftTransitionBuilder(FlowState.Main initialState, FeatureToggleService featureToggleService) {
        super(initialState, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(CLAIM_SUBMITTED)
            .onlyWhen(claimSubmittedOneRespondentRepresentative.or(claimSubmitted1v1RespondentOneUnregistered))
            .set(flags -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.of(
                    FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), true,
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                    DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardServiceEnabled(),
                    BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                )))
            .moveTo(CLAIM_SUBMITTED)
            .onlyWhen(claimSubmittedTwoRegisteredRespondentRepresentatives
                .or(claimSubmittedTwoRespondentRepresentativesOneUnregistered)
                .or(claimSubmittedBothUnregisteredSolicitors))
            .set(flags -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.of(
                    FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false,
                    FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true,
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                    DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardServiceEnabled(),
                    BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                )))
            // Only one unrepresented defendant
            .moveTo(CLAIM_SUBMITTED)
            .onlyWhen(claimSubmittedOneUnrepresentedDefendantOnly)
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                    DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardServiceEnabled(),
                    BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                )))
            // Unrepresented defendant 1
            .moveTo(CLAIM_SUBMITTED)
            .onlyWhen(claimSubmittedRespondent1Unrepresented
                .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())
                .and(claimSubmittedRespondent2Unrepresented.negate()))
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                    FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false,
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                    DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardServiceEnabled(),
                    BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                )))
            // Unrepresented defendant 2
            .moveTo(CLAIM_SUBMITTED)
            .onlyWhen(claimSubmittedRespondent2Unrepresented
                .and(claimSubmittedRespondent1Unrepresented.negate()))
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false,
                    FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true,
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                    DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardServiceEnabled(),
                    BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                )))
            // Unrepresented defendants
            .moveTo(CLAIM_SUBMITTED)
            .onlyWhen(claimSubmittedRespondent1Unrepresented.and(
                claimSubmittedRespondent2Unrepresented))
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                    FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true,
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                    DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardServiceEnabled(),
                    BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                )));
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
            && (caseData.getAddRespondent2() == YES && caseData.getRespondent2OrgRegistered() == NO
            && (caseData.getRespondent2SameLegalRepresentative() == NO
            || caseData.getRespondent2SameLegalRepresentative() == null));
}
