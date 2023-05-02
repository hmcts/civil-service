package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.CreateFlowNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.SetNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionTo;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.GENERAL_APPLICATION_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothRespondentUnrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothUnregisteredSolicitors;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneUnrepresentedDefendantOnly;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOnlyOneRespondentRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent1Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent2Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRegisteredRespondentRepresentatives;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRespondentRepresentativesOneUnregistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.noticeOfChangeEnabled;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;

@Component
@RequiredArgsConstructor
public class InitialStateTransitions {

    private final FeatureToggleService featureToggleService;

    private SetNext<FlowState.Main> toClaimSubmittedOneRespondentRepresentative(
        TransitionTo<FlowState.Main> builder) {
        return builder.transitionTo(CLAIM_SUBMITTED)
            .onlyIf(claimSubmittedOneRespondentRepresentative)
            .set(flags -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.of(
                    FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), true,
                    FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                    FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                    FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled(),
                    FlowFlag.CERTIFICATE_OF_SERVICE.name(), featureToggleService.isCertificateOfServiceEnabled(),
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled()
                )));
    }

    private SetNext<FlowState.Main> toClaimSubmittedTwoRegisteredRespondentRepresentatives(TransitionTo<FlowState.Main> builder) {
        return builder.transitionTo(CLAIM_SUBMITTED)
            .onlyIf(claimSubmittedTwoRegisteredRespondentRepresentatives
                        .or(claimSubmittedTwoRespondentRepresentativesOneUnregistered))
            .set(flags -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.of(
                    FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false,
                    FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true,
                    FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                    FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                    FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled(),
                    FlowFlag.CERTIFICATE_OF_SERVICE.name(), featureToggleService.isCertificateOfServiceEnabled(),
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled()
                )));
    }

    private SetNext<FlowState.Main> toClaimSubmittedOnNoticeOfChangeDisabled(TransitionTo<FlowState.Main> builder) {
        // To be removed when NOC is released. Needed for cases with unregistered and unrepresented defendants
        return builder.transitionTo(CLAIM_SUBMITTED)
            .onlyIf(noticeOfChangeEnabled.negate()
                        .and((claimSubmittedBothRespondentUnrepresented
                            .or(claimSubmittedOnlyOneRespondentRepresented)
                            .or(claimSubmittedBothUnregisteredSolicitors)
                            // this line MUST be removed when NOC toggle(noticeOfChangeEnabledAndLiP) is removed
                            .or(claimSubmittedOneUnrepresentedDefendantOnly))))
            .set(flags -> flags.putAll(
                // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                // camunda diagram for TAKE_CASE_OFFLINE is changed
                Map.of(
                    FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                    FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                    FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled(),
                    FlowFlag.CERTIFICATE_OF_SERVICE.name(), featureToggleService.isCertificateOfServiceEnabled(),
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled()
                )));
    }

    private SetNext<FlowState.Main> toClaimSubmittedOnNoticeOfChangeEnabledAndOneUnrepresentedDefendantOnly(TransitionTo<FlowState.Main> builder) {
        // Only one unrepresented defendant
        return builder.transitionTo(CLAIM_SUBMITTED)
            .onlyIf(noticeOfChangeEnabled.and(claimSubmittedOneUnrepresentedDefendantOnly))
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                    FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                    FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                    FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled(),
                    FlowFlag.CERTIFICATE_OF_SERVICE.name(), featureToggleService.isCertificateOfServiceEnabled(),
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled()
                )));
    }

    private SetNext<FlowState.Main> toClaimSubmittedOnNoticeOfChangeEnabledAndUnrepresentedDefendant1(TransitionTo<FlowState.Main> builder) {
        // Unrepresented defendant 1
        return builder.transitionTo(CLAIM_SUBMITTED)
            .onlyIf(noticeOfChangeEnabled
                        .and(claimSubmittedRespondent1Unrepresented)
                        .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())
                        .and(claimSubmittedRespondent2Unrepresented.negate()))
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                    FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false,
                    FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                    FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                    FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled(),
                    FlowFlag.CERTIFICATE_OF_SERVICE.name(), featureToggleService.isCertificateOfServiceEnabled(),
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled()
                )));
    }

    private SetNext<FlowState.Main> toClaimSubmittedOnNoticeOfChangeEnabledAndUnrepresentedDefendant2(TransitionTo<FlowState.Main> builder) {
        // Unrepresented defendant 2
        return builder.transitionTo(CLAIM_SUBMITTED)
            .onlyIf(noticeOfChangeEnabled
                        .and(claimSubmittedRespondent2Unrepresented
                                 .and(claimSubmittedRespondent1Unrepresented.negate())))
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false,
                    FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true,
                    FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                    FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                    FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled(),
                    FlowFlag.CERTIFICATE_OF_SERVICE.name(), featureToggleService.isCertificateOfServiceEnabled(),
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled()
                )));
    }

    private SetNext<FlowState.Main> toClaimSubmittedOnNoticeOfChangeEnabledAndUnrepresentedDefendants(TransitionTo<FlowState.Main> builder) {
        // Unrepresented defendants
        return builder.transitionTo(CLAIM_SUBMITTED)
            .onlyIf(noticeOfChangeEnabled.and(claimSubmittedRespondent1Unrepresented.and(
                claimSubmittedRespondent2Unrepresented)))
            .set(flags -> flags.putAll(
                Map.of(
                    FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                    FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true,
                    FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                    FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                    FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled(),
                    FlowFlag.CERTIFICATE_OF_SERVICE.name(), featureToggleService.isCertificateOfServiceEnabled(),
                    GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled()
                )));
    }

    public State<FlowState.Main> defineTransitions(CreateFlowNext<FlowState.Main> flow, FlowState.Main initialState) {
        TransitionTo<FlowState.Main> builder = flow.initial(initialState);
        SetNext<FlowState.Main> next = toClaimSubmittedOneRespondentRepresentative(builder);
        next = toClaimSubmittedTwoRegisteredRespondentRepresentatives(next);
        next = toClaimSubmittedOnNoticeOfChangeDisabled(next);
        next = toClaimSubmittedOnNoticeOfChangeEnabledAndOneUnrepresentedDefendantOnly(next);
        next = toClaimSubmittedOnNoticeOfChangeEnabledAndUnrepresentedDefendant1(next);
        next = toClaimSubmittedOnNoticeOfChangeEnabledAndUnrepresentedDefendant2(next);
        next = toClaimSubmittedOnNoticeOfChangeEnabledAndUnrepresentedDefendants(next);
        return next;
    }
}
