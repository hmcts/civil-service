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
public class PartAdmitNotSettleNoMediationTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        PartAdmitNotSettleNoMediationTransitionBuilder builder = new PartAdmitNotSettleNoMediationTransitionBuilder(
            mockFeatureToggleService);
        result = builder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(5);

        assertTransition(result.get(0), "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION", "MAIN.IN_HEARING_READINESS");
        assertTransition(result.get(1), "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION", "MAIN.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE");
        assertTransition(result.get(2), "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(3), "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION", "MAIN.TAKEN_OFFLINE_AFTER_SDO");
        assertTransition(result.get(4), "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION", "MAIN.TAKEN_OFFLINE_SDO_NOT_DRAWN");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
