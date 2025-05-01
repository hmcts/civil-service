package uk.gov.hmcts.reform.civil.stateflow.transitions;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmitted1v1RespondentOneUnregistered;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedBothUnregisteredSolicitors;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedOneUnrepresentedDefendantOnly;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedRespondent1Unrepresented;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedRespondent2Unrepresented;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedTwoRegisteredRespondentRepresentatives;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedTwoRespondentRepresentativesOneUnregistered;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class DraftTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        DraftTransitionBuilder draftTransitionBuilder = new DraftTransitionBuilder(
            FlowState.Main.DRAFT,
            mockFeatureToggleService
        ) {
        };
        result = draftTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(6);

        assertTransition(result.get(0), "MAIN.DRAFT", "MAIN.CLAIM_SUBMITTED");
        assertTransition(result.get(1), "MAIN.DRAFT", "MAIN.CLAIM_SUBMITTED");
        assertTransition(result.get(2), "MAIN.DRAFT", "MAIN.CLAIM_SUBMITTED");
        assertTransition(result.get(3), "MAIN.DRAFT", "MAIN.CLAIM_SUBMITTED");
        assertTransition(result.get(4), "MAIN.DRAFT", "MAIN.CLAIM_SUBMITTED");
        assertTransition(result.get(5), "MAIN.DRAFT", "MAIN.CLAIM_SUBMITTED");
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        assertThat(getCaseFlags(result.get(0), caseData)).hasSize(11).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), false),
            entry(FlowFlag.CASE_PROGRESSION_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("ONE_RESPONDENT_REPRESENTATIVE", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.BILINGUAL_DOCS.name(), false)
        );
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        assertThat(getCaseFlags(result.get(0), caseData)).hasSize(11).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), false),
            entry(FlowFlag.CASE_PROGRESSION_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("ONE_RESPONDENT_REPRESENTATIVE", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.BILINGUAL_DOCS.name(), false)
        );
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
        assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtDraftState1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent2Represented(YES)
            .respondent2OrgRegistered(YES)
            .build();
        assertTrue(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedBothUnregisteredSolicitors.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesUnregisteredState() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered()
            .build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertTrue(claimSubmittedBothUnregisteredSolicitors.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesSameSolicitorNullUnregistered() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered()
            .respondent2SameLegalRepresentative(null)
            .build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertTrue(claimSubmittedBothUnregisteredSolicitors.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtDraftState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertFalse(claimSubmittedBothUnregisteredSolicitors.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRepresentativesStateRespOneUnreg() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent2Represented(YES)
            .respondent2OrgRegistered(YES)
            .respondent1OrgRegistered(NO)
            .respondent2SameLegalRepresentative(NO)
            .build();
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertTrue(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertThat(getCaseFlags(result.get(1), caseData)).hasSize(12).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), false),
            entry(FlowFlag.CASE_PROGRESSION_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("ONE_RESPONDENT_REPRESENTATIVE", false),
            entry("TWO_RESPONDENT_REPRESENTATIVES", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.BILINGUAL_DOCS.name(), false)
        );
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRepresentativesStateRespTwoUnreg() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent2Represented(YES)
            .respondent2OrgRegistered(NO)
            .respondent1OrgRegistered(YES)
            .respondent2SameLegalRepresentative(NO)
            .build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertTrue(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedBothRepresentativesUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent1Represented(NO)
            .respondent2Represented(NO)
            .build();
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);
        when(mockFeatureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);

        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
        assertTrue(claimSubmittedRespondent2Unrepresented.test(caseData));
        assertThat(getCaseFlags(result.get(5), caseData)).hasSize(12).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), true),
            entry(FlowFlag.CASE_PROGRESSION_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("UNREPRESENTED_DEFENDANT_ONE", true),
            entry("UNREPRESENTED_DEFENDANT_TWO", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), true),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.BILINGUAL_DOCS.name(), false)
        );
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedFirstRepresentativeUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent1Represented(YES)
            .respondent2Represented(NO)
            .build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertTrue(claimSubmittedRespondent2Unrepresented.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenRespondentSolicitorUnregistered() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedRespondent1Unregistered()
            .addRespondent2(NO)
            .build();
        assertTrue(claimSubmitted1v1RespondentOneUnregistered.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondentSolicitorRegistered() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertFalse(claimSubmitted1v1RespondentOneUnregistered.test(caseData));
    }

    @Test
    void shouldResolve_whenOnlyOneUnrepresentedDefendant() {
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);
        when(mockFeatureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(mockFeatureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1UnrepresentedDefendant()
            .defendant1LIPAtClaimIssued(YES).build();

        assertTrue(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
        assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
        assertThat(getCaseFlags(result.get(2), caseData)).hasSize(11).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), true),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), true),
            entry(FlowFlag.CASE_PROGRESSION_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("UNREPRESENTED_DEFENDANT_ONE", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), true),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.BILINGUAL_DOCS.name(), false)
        );
    }

    @Test
    void shouldResolve_whenFirstDefendantUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendant1()
            .defendant1LIPAtClaimIssued(YES).build();

        assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
        assertFalse(claimSubmittedRespondent2Unrepresented.test(caseData));
    }

    @Test
    void shouldResolve_whenSecondDefendantUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedUnrepresentedDefendant2()
            .defendant2LIPAtClaimIssued(YES).build();
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        assertFalse(claimSubmittedRespondent1Unrepresented.test(caseData));
        assertTrue(claimSubmittedRespondent2Unrepresented.test(caseData));
        assertThat(getCaseFlags(result.get(3), caseData)).hasSize(12).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), true),
            entry(FlowFlag.CASE_PROGRESSION_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("UNREPRESENTED_DEFENDANT_ONE", true),
            entry("UNREPRESENTED_DEFENDANT_TWO", false),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.BILINGUAL_DOCS.name(), false)
        );
    }

    @Test
    void shouldResolve_whenBothDefendantsUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .defendant1LIPAtClaimIssued(YES)
            .defendant2LIPAtClaimIssued(YES)
            .atStateClaimIssuedUnrepresentedDefendants().build();

        assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
        assertTrue(claimSubmittedRespondent2Unrepresented.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }

    private Map<String, Boolean> getCaseFlags(Transition result, CaseData caseData) {
        Map<String, Boolean> flags = new HashMap<>();
        result.getDynamicFlags().accept(caseData, flags);
        return flags;
    }
}
