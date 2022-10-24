package uk.gov.hmcts.reform.civil.handler.event;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@ExtendWith(SpringExtension.class)
class DecisionOutcomeEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private DecisionOutcomeEventHandler handler;

    @Test
    void shouldCallMoveToDecisionOutcomeEventWithExpectedParams_WhenDismissClaimEvent() {
        DismissClaimEvent event = new DismissClaimEvent(1L);

        handler.moveCaseToDecisionOutcome(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), MOVE_TO_DECISION_OUTCOME);
    }

}
