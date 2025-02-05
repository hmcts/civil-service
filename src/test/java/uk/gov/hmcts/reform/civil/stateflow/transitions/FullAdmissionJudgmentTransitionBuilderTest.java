package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FullAdmissionJudgmentTransitionBuilderTest {
    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        FullAdmissionJudgmentTransitionBuilder fullAdmissionJudgmentTransitionBuilder =
            new FullAdmissionJudgmentTransitionBuilder(mockFeatureToggleService);
        result = fullAdmissionJudgmentTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(1);

        assertTransition(result.get(0), "MAIN.FULL_ADMIT_JUDGMENT_ADMISSION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
