package uk.gov.hmcts.reform.unspec.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.event.MoveCaseToStuckOutEvent;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MOVE_CLAIM_TO_STRUCK_OUT;

@ExtendWith(SpringExtension.class)
class MoveCaseToStruckOutEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private MoveCaseToStruckOutEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenMoveToStayedEvent() {
        MoveCaseToStuckOutEvent event = new MoveCaseToStuckOutEvent(1L);

        handler.moveCaseToStruckOut(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), MOVE_CLAIM_TO_STRUCK_OUT);
    }

}
