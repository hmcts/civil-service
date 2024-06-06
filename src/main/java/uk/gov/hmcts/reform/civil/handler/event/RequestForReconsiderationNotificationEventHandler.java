package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.RequestForReconsiderationNotificationDeadlineEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestForReconsiderationNotificationEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void triggerNotificationDeletionProcess(RequestForReconsiderationNotificationDeadlineEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK);
    }
}
