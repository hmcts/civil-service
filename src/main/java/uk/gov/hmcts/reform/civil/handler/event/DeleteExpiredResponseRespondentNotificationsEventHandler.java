package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.DeleteExpiredResponseRespondentNotificationsEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT_RESPONSE_DEADLINE_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteExpiredResponseRespondentNotificationsEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void triggerNotificationDeletionProcess(DeleteExpiredResponseRespondentNotificationsEvent event) {
        log.info("{} for caseId: {}", event, event.caseId());
        coreCaseDataService.triggerGaEvent(event.caseId(), RESPONDENT_RESPONSE_DEADLINE_CHECK);
    }
}
