package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.event.BreathingSpaceLiftPendingEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_PENDING_BREATHING_SPACE_LIFT;

@ExtendWith(MockitoExtension.class)
class BreathingSpaceLiftPendingEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private BreathingSpaceLiftPendingEventHandler handler;

    @Test
    void shouldTriggerApplyPendingBreathingSpaceLift() {
        Long caseId = 1633357679902210L;
        BreathingSpaceLiftPendingEvent event = new BreathingSpaceLiftPendingEvent(caseId);

        handler.applyPendingBreathingSpaceLift(event);

        verify(coreCaseDataService).triggerEvent(caseId, APPLY_PENDING_BREATHING_SPACE_LIFT);
    }
}
