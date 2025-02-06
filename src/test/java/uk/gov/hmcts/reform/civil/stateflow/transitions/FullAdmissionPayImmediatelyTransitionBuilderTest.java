package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FullAdmissionPayImmediatelyTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        FullAdmissionPayImmediatelyTransitionBuilder fullAdmissionPayImmediatelyTransitionBuilder =
            new FullAdmissionPayImmediatelyTransitionBuilder(mockFeatureToggleService);
        result = fullAdmissionPayImmediatelyTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(2);

        assertTransition(result.get(0), "MAIN.FULL_ADMIT_PAY_IMMEDIATELY", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(1), "MAIN.FULL_ADMIT_PAY_IMMEDIATELY", "MAIN.FULL_ADMIT_JUDGMENT_ADMISSION");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
