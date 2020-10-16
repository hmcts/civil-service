package uk.gov.hmcts.reform.unspec.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.event.MoveCaseToStayedEvent;
import uk.gov.hmcts.reform.unspec.handler.event.MoveCaseToStayedEventHandler;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class MoveCaseToStayedEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private MoveCaseToStayedEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenMoveToStayedEvent() {
        MoveCaseToStayedEvent event = new MoveCaseToStayedEvent(1L);

        handler.moveCaseToStayed(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), CaseEvent.MOVE_TO_STAYED);
    }
}
