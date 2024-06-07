package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void shouldCorrectlyAssignSourceAndTargetStates() {
        assertThat(result).isNotEmpty();

        for (Transition transition : result) {
            assertTransition(transition, transition.getSourceState(), transition.getTargetState());
        }
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
