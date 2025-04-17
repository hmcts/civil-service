package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;

@Nested
class ClaimDismissedAllLegalRepsEmailGeneratorTest {

    @Mock
    private ClaimDismissedAppSolOneEmailDTOGenerator appSolOneEmailGenerator;

    @Mock
    private ClaimDismissedRespSolOneEmailDTOGenerator respSolOneEmailGenerator;

    @Mock
    private ClaimDismissedRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @InjectMocks
    private ClaimDismissedAllLegalRepsEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyRespondents_whenStateIsClaimDismissedPastDeadline() {
        CaseData caseData = mock(CaseData.class);
        StateFlow stateFlow = mock(StateFlow.class);
        State state = mock(State.class);

        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.getState()).thenReturn(state);
        when(state.getName()).thenReturn(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());

        boolean result = emailGenerator.shouldNotifyRespondents(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotNotifyRespondents_whenStateIsNotClaimDismissedPastDeadline() {
        CaseData caseData = mock(CaseData.class);
        StateFlow stateFlow = mock(StateFlow.class);
        State state = mock(State.class);

        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.getState()).thenReturn(state);
        when(state.getName()).thenReturn("some-other-state");

        boolean result = emailGenerator.shouldNotifyRespondents(caseData);

        assertThat(result).isFalse();
    }
}

