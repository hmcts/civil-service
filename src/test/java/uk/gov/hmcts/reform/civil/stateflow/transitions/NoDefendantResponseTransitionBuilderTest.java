package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NoDefendantResponseTransitionBuilder.caseDismissedAfterDetailNotifiedExtension;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NoDefendantResponseTransitionBuilder.takenOfflineByStaffAfterNoResponse;

@ExtendWith(MockitoExtension.class)
public class NoDefendantResponseTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        NoDefendantResponseTransitionBuilder noDefendantResponseTransitionBuilder =
            new NoDefendantResponseTransitionBuilder(mockFeatureToggleService);
        result = noDefendantResponseTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(3);

        assertTransition(result.get(0), "MAIN.NO_DEFENDANT_RESPONSE", "MAIN.IN_HEARING_READINESS");
        assertTransition(result.get(1), "MAIN.NO_DEFENDANT_RESPONSE", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(2), "MAIN.NO_DEFENDANT_RESPONSE", "MAIN.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA");
    }

    @Test
    void shouldReturnFalse_whenNotTakenOfflineByStaff() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(null)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .takenOfflineByStaffDate(null)
            .build();

        assertFalse(takenOfflineByStaffAfterNoResponse.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaff() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(null)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();

        assertTrue(takenOfflineByStaffAfterNoResponse.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenNotCaseDismissedAfterDetailNotifiedExtension() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(null)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .claimDismissedDeadline(LocalDateTime.now().plusDays(1))
            .build();

        assertFalse(caseDismissedAfterDetailNotifiedExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDismissedAfterDetailNotifiedExtension() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(null)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .claimDismissedDeadline(LocalDateTime.now())
            .build();

        assertTrue(caseDismissedAfterDetailNotifiedExtension.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
