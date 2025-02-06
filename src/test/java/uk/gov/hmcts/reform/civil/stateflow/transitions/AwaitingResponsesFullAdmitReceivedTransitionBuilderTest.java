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
public class AwaitingResponsesFullAdmitReceivedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;
    private AwaitingResponsesFullAdmitReceivedTransitionBuilder awaitingResponsesFullAdmitReceivedTransitionBuilder;

    @BeforeEach
    void setUp() {
        awaitingResponsesFullAdmitReceivedTransitionBuilder = new AwaitingResponsesFullAdmitReceivedTransitionBuilder(mockFeatureToggleService);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        List<Transition> result = awaitingResponsesFullAdmitReceivedTransitionBuilder.buildTransitions();
        assertNotNull(result);
        assertThat(result).hasSize(4);

        assertTransition(result.get(0), "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED", "MAIN.ALL_RESPONSES_RECEIVED");
        assertTransition(result.get(1), "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED", "MAIN.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED");
        assertTransition(result.get(2), "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(3), "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED", "MAIN.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
