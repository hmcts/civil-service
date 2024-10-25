package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedOneUnrepresentedDefendantOnly;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedRespondent1Unrepresented;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.DraftTransitionBuilder.claimSubmittedRespondent2Unrepresented;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PendingClaimIssuedUnrepresentedDefendantTransitionBuilder.certificateOfServiceEnabled;

@ExtendWith(MockitoExtension.class)
public class PendingClaimIssuedUnrepresentedDefendantTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        PendingClaimIssuedUnrepresentedDefendantTransitionBuilder builder = new PendingClaimIssuedUnrepresentedDefendantTransitionBuilder(
            mockFeatureToggleService);
        result = builder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(2);

        assertTransition(result.get(0), "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT", "MAIN.CLAIM_ISSUED");
        assertTransition(result.get(1), "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT", "MAIN.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT");
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        assertFalse(certificateOfServiceEnabled.test(caseData));
    }

    @Test
    void shouldReturnFalse_cos_whenCaseDataAtClaimSubmittedState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertFalse(certificateOfServiceEnabled.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
        assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        assertFalse(certificateOfServiceEnabled.test(caseData));
    }

    @Test
    void shouldReturnFalse_cos_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
        assertFalse(certificateOfServiceEnabled.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
        assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
        assertFalse(certificateOfServiceEnabled.test(caseData));
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

    @Test
    void shouldReturnTrue_whenEitherDefendant1LIPAtClaimIssuedOrDefendant2LIPAtClaimIssuedIsYes() {
        CaseData caseData1 = CaseData.builder()
            .defendant1LIPAtClaimIssued(YES)
            .build();

        assertTrue(certificateOfServiceEnabled.test(caseData1));

        CaseData caseData2 = CaseData.builder()
            .defendant2LIPAtClaimIssued(YES)
            .build();

        assertTrue(certificateOfServiceEnabled.test(caseData2));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
