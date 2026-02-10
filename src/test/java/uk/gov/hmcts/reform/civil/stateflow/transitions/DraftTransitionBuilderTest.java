package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimPredicate;
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
        assertTrue(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
        assertThat(getCaseFlags(result.get(0), caseData)).hasSize(10).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("ONE_RESPONDENT_REPRESENTATIVE", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.JBA_ISSUED_BEFORE_NOC.name(), false),
            entry(FlowFlag.IS_CJES_SERVICE_ENABLED.name(), false)
        );
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        assertTrue(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
        assertThat(getCaseFlags(result.get(0), caseData)).hasSize(10).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("ONE_RESPONDENT_REPRESENTATIVE", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.JBA_ISSUED_BEFORE_NOC.name(), false),
            entry(FlowFlag.IS_CJES_SERVICE_ENABLED.name(), false)
        );
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
        assertFalse(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtDraftState1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        assertFalse(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent2Represented(YES)
            .respondent2OrgRegistered(YES)
            .build();
        assertTrue(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesUnregisteredState() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered()
            .build();
        assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertTrue(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesSameSolicitorNullUnregistered() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered()
            .respondent2SameLegalRepresentative(null)
            .build();
        assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertTrue(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtDraftState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertFalse(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
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

        assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertTrue(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertThat(getCaseFlags(result.get(1), caseData)).hasSize(11).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), false),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("ONE_RESPONDENT_REPRESENTATIVE", false),
            entry("TWO_RESPONDENT_REPRESENTATIVES", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.JBA_ISSUED_BEFORE_NOC.name(), false),
            entry(FlowFlag.IS_CJES_SERVICE_ENABLED.name(), false)
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
        assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertTrue(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedBothRepresentativesUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent1Represented(NO)
            .respondent2Represented(NO)
            .build();
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);
        when(mockFeatureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);

        assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertTrue(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        assertTrue(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
        assertThat(getCaseFlags(result.get(5), caseData)).hasSize(11).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), true),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("UNREPRESENTED_DEFENDANT_ONE", true),
            entry("UNREPRESENTED_DEFENDANT_TWO", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), true),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.JBA_ISSUED_BEFORE_NOC.name(), false),
            entry(FlowFlag.IS_CJES_SERVICE_ENABLED.name(), false)
        );
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedFirstRepresentativeUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent1Represented(YES)
            .respondent2Represented(NO)
            .build();
        assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertTrue(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenRespondentSolicitorUnregistered() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedRespondent1Unregistered()
            .addRespondent2(NO)
            .build();
        assertTrue(ClaimPredicate.submitted1v1RespondentOneUnregistered.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondentSolicitorRegistered() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertFalse(ClaimPredicate.submitted1v1RespondentOneUnregistered.test(caseData));
    }

    @Test
    void shouldResolve_whenOnlyOneUnrepresentedDefendant() {
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);
        when(mockFeatureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
        when(mockFeatureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1UnrepresentedDefendant()
            .defendant1LIPAtClaimIssued(YES).build();

        assertTrue(ClaimPredicate.submittedOneUnrepresentedDefendantOnly.test(caseData));
        assertTrue(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        assertThat(getCaseFlags(result.get(2), caseData)).hasSize(10).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), true),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), true),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("UNREPRESENTED_DEFENDANT_ONE", true),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), true),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.JBA_ISSUED_BEFORE_NOC.name(), false),
            entry(FlowFlag.IS_CJES_SERVICE_ENABLED.name(), false)
        );
    }

    @Test
    void shouldResolve_whenFirstDefendantUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendant1()
            .defendant1LIPAtClaimIssued(YES).build();

        assertTrue(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        assertFalse(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
    }

    @Test
    void shouldResolve_whenSecondDefendantUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedUnrepresentedDefendant2()
            .defendant2LIPAtClaimIssued(YES).build();
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        assertFalse(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        assertTrue(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
        assertThat(getCaseFlags(result.get(3), caseData)).hasSize(11).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), true),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), false),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("UNREPRESENTED_DEFENDANT_ONE", true),
            entry("UNREPRESENTED_DEFENDANT_TWO", false),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), false),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.JBA_ISSUED_BEFORE_NOC.name(), false),
            entry(FlowFlag.IS_CJES_SERVICE_ENABLED.name(), false)
        );
    }

    @Test
    void shouldSetFlags_whenOnlyRespondent2IsUnrepresented() {
        // Covers the dedicated "Unrepresented defendant 2" path (transition index 4)
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent1Represented(YES) // respondent 1 is represented
            .respondent2Represented(NO)  // respondent 2 is unrepresented
            .build();

        // Guards
        assertFalse(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        assertTrue(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));

        Map<String, Boolean> flags = getCaseFlags(result.get(4), caseData);
        assertThat(flags).contains(
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false),
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true)
        );
    }

    @Test
    void shouldResolve_whenBothDefendantsUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .defendant1LIPAtClaimIssued(YES)
            .defendant2LIPAtClaimIssued(YES)
            .atStateClaimIssuedUnrepresentedDefendants().build();

        assertTrue(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        assertTrue(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
    }

    @Test
    void shouldResolve_whenUnrepresentedDefendantWithJudgmentByAdmissionForNoc() {
        when(mockFeatureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);
        when(mockFeatureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(mockFeatureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(NO)
            .specRespondent1Represented(NO)
            .applicant1Represented(NO)
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .activeJudgment(new JudgmentDetails()
                                .setType(JudgmentType.JUDGMENT_BY_ADMISSION))
            .build();

        assertThat(getCaseFlags(result.get(3), caseData)).hasSize(11).contains(
            entry(FlowFlag.BULK_CLAIM_ENABLED.name(), false),
            entry(FlowFlag.DASHBOARD_SERVICE_ENABLED.name(), true),
            entry(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), true),
            entry(FlowFlag.IS_JO_LIVE_FEED_ACTIVE.name(), false),
            entry("UNREPRESENTED_DEFENDANT_ONE", true),
            entry("UNREPRESENTED_DEFENDANT_TWO", false),
            entry(FlowFlag.DEFENDANT_NOC_ONLINE.name(), true),
            entry(FlowFlag.WELSH_ENABLED.name(), false),
            entry(FlowFlag.JBA_ISSUED_BEFORE_NOC.name(), true),
            entry(FlowFlag.IS_CJES_SERVICE_ENABLED.name(), false)
        );
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
