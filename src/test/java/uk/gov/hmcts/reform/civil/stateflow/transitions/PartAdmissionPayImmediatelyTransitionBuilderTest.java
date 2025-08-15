package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PartAdmissionPayImmediatelyTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        PartAdmissionPayImmediatelyTransitionBuilder partAdmissionPayImmediatelyTransitionBuilder =
            new PartAdmissionPayImmediatelyTransitionBuilder(mockFeatureToggleService);
        result = partAdmissionPayImmediatelyTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(2);

        assertTransition(result.get(0), "MAIN.PART_ADMIT_PAY_IMMEDIATELY", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(1), "MAIN.PART_ADMIT_PAY_IMMEDIATELY", "MAIN.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
