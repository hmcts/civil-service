package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.civil.exceptions.GaNotificationHistoryException;
import uk.gov.hmcts.reform.civil.notification.handlers.NotificationSummaryParser;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_NOTIFICATIONS;

@Service
@RequiredArgsConstructor
public class GaNotificationHistoryService {

    private final GaCoreCaseDataService coreCaseDataService;

    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void recordNotifications(String caseId, String fullSummary) {
        var start = coreCaseDataService.startGaUpdate(caseId, RECORD_NOTIFICATIONS);
        var data = start.getCaseDetails().getData();
        data.put("notificationSummary", null);
        var event = Event.builder().id(start.getEventId()).summary(NotificationSummaryParser.attempted(fullSummary));
        String errors = NotificationSummaryParser.errors(fullSummary);
        if (errors != null) {
            event.description("Errors: " + errors);
        }
        coreCaseDataService.submitGaUpdate(caseId, CaseDataContent.builder()
            .eventToken(start.getToken()).event(event.build()).data(data).build());
    }

    @Recover
    public void recover(RuntimeException exception, String caseId, String fullSummary) {
        throw new GaNotificationHistoryException(caseId, exception);
    }
}
