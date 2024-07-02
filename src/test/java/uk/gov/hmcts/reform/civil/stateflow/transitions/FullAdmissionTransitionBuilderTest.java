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
public class FullAdmissionTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        FullAdmissionTransitionBuilder builder = new FullAdmissionTransitionBuilder(mockFeatureToggleService);
        result = builder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(8);

        assertTransition(result.get(0), "MAIN.FULL_ADMISSION", "MAIN.FULL_ADMIT_PAY_IMMEDIATELY");
        assertTransition(result.get(1), "MAIN.FULL_ADMISSION", "MAIN.FULL_ADMIT_PROCEED");
        assertTransition(result.get(2), "MAIN.FULL_ADMISSION", "MAIN.FULL_ADMIT_NOT_PROCEED");
        assertTransition(result.get(3), "MAIN.FULL_ADMISSION", "MAIN.FULL_ADMIT_AGREE_REPAYMENT");
        assertTransition(result.get(4), "MAIN.FULL_ADMISSION", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");
        assertTransition(result.get(5), "MAIN.FULL_ADMISSION", "MAIN.FULL_ADMIT_JUDGMENT_ADMISSION");
        assertTransition(result.get(6), "MAIN.FULL_ADMISSION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(7), "MAIN.FULL_ADMISSION", "MAIN.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
