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
public class ContactDetailsChangeTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        ContactDetailsChangeTransitionBuilder contactDetailsChangeTransitionBuilder =
            new ContactDetailsChangeTransitionBuilder(mockFeatureToggleService);
        result = contactDetailsChangeTransitionBuilder.buildTransitions();
        assertNotNull(result);

    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(5);

        assertTransition(result.get(0), "MAIN.CONTACT_DETAILS_CHANGE", "MAIN.FULL_DEFENCE");
        assertTransition(result.get(1), "MAIN.CONTACT_DETAILS_CHANGE", "MAIN.PART_ADMISSION");
        assertTransition(result.get(2), "MAIN.CONTACT_DETAILS_CHANGE", "MAIN.FULL_ADMISSION");
        assertTransition(result.get(3), "MAIN.CONTACT_DETAILS_CHANGE", "MAIN.COUNTER_CLAIM");
        assertTransition(result.get(4), "MAIN.CONTACT_DETAILS_CHANGE", "MAIN.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
