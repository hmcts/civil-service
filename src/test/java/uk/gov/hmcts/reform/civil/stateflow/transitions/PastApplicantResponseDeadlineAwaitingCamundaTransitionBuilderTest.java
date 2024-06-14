package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PastApplicantResponseDeadlineAwaitingCamundaTransitionBuilder.applicantOutOfTimeProcessedByCamunda;

@ExtendWith(MockitoExtension.class)
public class PastApplicantResponseDeadlineAwaitingCamundaTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        PastApplicantResponseDeadlineAwaitingCamundaTransitionBuilder builder =
            new PastApplicantResponseDeadlineAwaitingCamundaTransitionBuilder(mockFeatureToggleService);
        result = builder.buildTransitions();
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(1);

        assertTransition(result.get(0), "MAIN.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA",
                         "MAIN.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE");
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateTakenOfflinePastApplicantResponseDeadline() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflinePastApplicantResponseDeadline()
            .build();
        assertTrue(applicantOutOfTimeProcessedByCamunda.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefenceAndProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build();
        assertFalse(applicantOutOfTimeProcessedByCamunda.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataTakenOffline() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflinePastApplicantResponseDeadline()
            .build();
        assertTrue(applicantOutOfTimeProcessedByCamunda.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataNotTakenOffline() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .build();
        assertFalse(applicantOutOfTimeProcessedByCamunda.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
