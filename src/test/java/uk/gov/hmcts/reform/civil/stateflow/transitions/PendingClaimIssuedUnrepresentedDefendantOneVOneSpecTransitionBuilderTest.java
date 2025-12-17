package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PendingClaimIssuedUnrepresentedDefendantOneVOneSpecTransitionBuilder.pinInPostEnabledAndLiP;

@ExtendWith(MockitoExtension.class)
public class PendingClaimIssuedUnrepresentedDefendantOneVOneSpecTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        PendingClaimIssuedUnrepresentedDefendantOneVOneSpecTransitionBuilder builder =
            new PendingClaimIssuedUnrepresentedDefendantOneVOneSpecTransitionBuilder(mockFeatureToggleService);
        result = builder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(2);

        assertTransition(result.get(0), "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC",
                         "MAIN.CLAIM_ISSUED");
        assertTransition(result.get(1), "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC",
                         "MAIN.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT");
    }

    @Test
    void shouldReturnTrue_whenRespondent1NotRepresentedPinToPostLR() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                               .respondentCaseRole("Solicitor")
                                               .build())
            .build();
        assertTrue(pinInPostEnabledAndLiP.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondent1PinToPostLRspecIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .build();
        assertFalse(pinInPostEnabledAndLiP.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondent1PinToPostLRspecIsEmpty() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .addRespondent1PinToPostLRspec(null)
            .build();
        assertFalse(pinInPostEnabledAndLiP.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondent1IsRepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.YES)
            .build();
        assertFalse(pinInPostEnabledAndLiP.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
