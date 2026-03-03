package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.event.DecisionOutcomeEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecisionOutcomeEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private DecisionOutcomeEventHandler handler;

    @Test
    void shouldCallMoveToDecisionOutcomeEventWithExpectedParams_WhenDismissClaimEvent() {
        DecisionOutcomeEvent event = new DecisionOutcomeEvent(1L);

        handler.moveCaseToDecisionOutcome(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), MOVE_TO_DECISION_OUTCOME);
    }

}
