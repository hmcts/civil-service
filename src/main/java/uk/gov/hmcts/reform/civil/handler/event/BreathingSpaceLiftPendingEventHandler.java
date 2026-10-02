package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.BreathingSpaceLiftPendingEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_PENDING_BREATHING_SPACE_LIFT;

@Slf4j
@Service
@RequiredArgsConstructor
public class BreathingSpaceLiftPendingEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void applyPendingBreathingSpaceLift(BreathingSpaceLiftPendingEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), APPLY_PENDING_BREATHING_SPACE_LIFT);
    }
}
