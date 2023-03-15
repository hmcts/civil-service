package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.TrialReadyNotificationEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_NOTIFICATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialReadyNotificationEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void sendTrialReadyNotification(TrialReadyNotificationEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), TRIAL_READY_NOTIFICATION);
    }
}
