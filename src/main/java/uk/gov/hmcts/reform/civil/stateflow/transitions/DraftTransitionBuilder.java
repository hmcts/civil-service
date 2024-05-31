package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.BULK_CLAIM_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DASHBOARD_SERVICE_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.GENERAL_APPLICATION_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmitted1v1RespondentOneUnregistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothUnregisteredSolicitors;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneUnrepresentedDefendantOnly;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent1Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent2Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRegisteredRespondentRepresentatives;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRespondentRepresentativesOneUnregistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;

@Component
@Scope(value = "prototype")
public class DraftTransitionBuilder extends TransitionBuilder {

    public DraftTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.DRAFT, featureToggleService);
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


}
