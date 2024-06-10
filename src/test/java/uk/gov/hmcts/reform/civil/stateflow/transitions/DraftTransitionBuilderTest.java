package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.certificateOfServiceEnabled;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmitted1v1RespondentOneUnregistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothRespondentUnrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothUnregisteredSolicitors;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneUnrepresentedDefendantOnly;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOnlyOneRespondentRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent1Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent2Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRegisteredRespondentRepresentatives;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRespondentRepresentativesOneUnregistered;

@ExtendWith(MockitoExtension.class)
public class DraftTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        DraftTransitionBuilder draftTransitionBuilder = new DraftTransitionBuilder(FlowState.Main.DRAFT, mockFeatureToggleService) {};
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
        assertFalse(certificateOfServiceEnabled.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
        assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        assertFalse(certificateOfServiceEnabled.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
        assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
        assertFalse(certificateOfServiceEnabled.test(caseData));
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
        assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesUnregisteredState() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered()
            .build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
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
        assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
        assertTrue(claimSubmittedBothUnregisteredSolicitors.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtDraftState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRepresentativesStateRespOneUnreg() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent2Represented(YES)
            .respondent2OrgRegistered(YES)
            .respondent1OrgRegistered(NO)
            .respondent2SameLegalRepresentative(NO)
            .build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
        assertTrue(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
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
        assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
        assertFalse(claimSubmittedOnlyOneRespondentRepresented.test(caseData));
        assertTrue(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedBothRepresentativesUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent1Represented(NO)
            .respondent2Represented(NO)
            .build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedOnlyOneRespondentRepresented.test(caseData));
        assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertTrue(claimSubmittedBothRespondentUnrepresented.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedFirstRepresentativeUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
            .respondent1Represented(YES)
            .respondent2Represented(NO)
            .build();
        assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
        assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
        assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        assertTrue(claimSubmittedOnlyOneRespondentRepresented.test(caseData));
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1UnrepresentedDefendant()
            .defendant1LIPAtClaimIssued(YES).build();

        assertTrue(certificateOfServiceEnabled.test(caseData));
        assertTrue(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
        assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
    }

    @Test
    void shouldResolve_whenFirstDefendantUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendant1()
            .defendant1LIPAtClaimIssued(YES).build();

        assertTrue(certificateOfServiceEnabled.test(caseData));
        assertFalse(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
        assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
        assertFalse(claimSubmittedRespondent2Unrepresented.test(caseData));
    }

    @Test
    void shouldResolve_whenSecondDefendantUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedUnrepresentedDefendant2()
            .defendant2LIPAtClaimIssued(YES).build();

        assertTrue(certificateOfServiceEnabled.test(caseData));
        assertFalse(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
        assertFalse(claimSubmittedRespondent1Unrepresented.test(caseData));
        assertTrue(claimSubmittedRespondent2Unrepresented.test(caseData));
    }

    @Test
    void shouldResolve_whenBothDefendantsUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .defendant1LIPAtClaimIssued(YES)
            .defendant2LIPAtClaimIssued(YES)
            .atStateClaimIssuedUnrepresentedDefendants().build();

        assertTrue(certificateOfServiceEnabled.test(caseData));
        assertFalse(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
        assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
        assertTrue(claimSubmittedRespondent2Unrepresented.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
