package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SpecDefendantNocTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        when(mockFeatureToggleService.isDefendantNoCOnline()).thenReturn(false);
        SpecDefendantNocTransitionBuilder builder = new SpecDefendantNocTransitionBuilder(
            mockFeatureToggleService);
        result = builder.buildTransitions();
        assertNotNull(result);
        assertThat(result).hasSize(1);
        assertTransition(result.get(0), "MAIN.SPEC_DEFENDANT_NOC", "MAIN.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC");
    }

    @Test
    void shouldStay_withDefendantNoCOnline() {
        when(mockFeatureToggleService.isDefendantNoCOnline()).thenReturn(true);
        SpecDefendantNocTransitionBuilder builder = new SpecDefendantNocTransitionBuilder(
            mockFeatureToggleService);
        result = builder.buildTransitions();
        assertNotNull(result);
        assertThat(result).hasSize(0);
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }

}
