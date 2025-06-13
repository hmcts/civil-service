package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FullAdmitAgreeRepaymentTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        FullAdmitAgreeRepaymentTransitionBuilder fullAdmitAgreeRepaymentTransitionBuilder =
                new FullAdmitAgreeRepaymentTransitionBuilder(mockFeatureToggleService);
        result = fullAdmitAgreeRepaymentTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(2);
        assertTransition(result.get(0), "MAIN.FULL_ADMIT_AGREE_REPAYMENT", "MAIN.SIGN_SETTLEMENT_AGREEMENT");
        assertTransition(result.get(1), "MAIN.FULL_ADMIT_AGREE_REPAYMENT", "MAIN.TAKEN_OFFLINE_BY_STAFF");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
