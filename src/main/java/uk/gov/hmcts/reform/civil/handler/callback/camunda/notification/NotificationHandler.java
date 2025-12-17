package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_NOTIFICATIONS;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationHandler extends CallbackHandler {

    private static final String ATTEMPTED_PREFIX = "Attempted: ";
    private static final String DELIMITER = " || Errors: ";

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT);

    private final NotifierFactory notifierFactory;
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendNotifications,
            callbackKey(SUBMITTED), this::recordNotifications
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return callbackParams.getCaseData().getBusinessProcess().getActivityId();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendNotifications(CallbackParams callbackParams) {
        final CaseData caseData = callbackParams.getCaseData();
        final String taskId = caseData.getBusinessProcess().getActivityId();
        final Notifier notifier = notifierFactory.getNotifier(taskId);

        final String summary = notifier.notifyParties(caseData, NOTIFY_EVENT.toString(), taskId);

        caseData.setNotificationSummary(summary);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse recordNotifications(CallbackParams callbackParams) {
        final CaseData caseData = callbackParams.getCaseData();
        final String caseId = caseData.getCcdCaseReference().toString();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, RECORD_NOTIFICATIONS);
        String fullSummary = caseData.getNotificationSummary();
        CaseDataContent caseContent = getCaseContent(startEventResponse, fullSummary);
        coreCaseDataService.submitUpdate(caseId, caseContent);
        return SubmittedCallbackResponse.builder().build();
    }

    private CaseDataContent getCaseContent(StartEventResponse startEventResponse, String fullSummary) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();

        data.put("notificationSummary", null);

        String cleanSummary = extractAttempted(fullSummary);
        String errors = extractErrors(fullSummary);

        Event.EventBuilder eventBuilder = Event.builder()
            .id(startEventResponse.getEventId())
            .summary(cleanSummary);

        if (errors != null && !errors.isEmpty()) {
            eventBuilder.description("Errors: " + errors);
        }

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(eventBuilder.build())
            .data(data)
            .build();
    }

    private String extractAttempted(String combined) {
        if (combined == null) {
            return null;
        }

        int delimiterPos = combined.indexOf(DELIMITER);
        String left = (delimiterPos >= 0) ? combined.substring(0, delimiterPos) : combined;

        if (left.startsWith(ATTEMPTED_PREFIX)) {
            return left.substring(ATTEMPTED_PREFIX.length());
        }

        return left;
    }

    private String extractErrors(String combined) {
        if (combined == null) {
            return null;
        }

        int delimiterPos = combined.indexOf(DELIMITER);
        if (delimiterPos < 0) {
            return null;
        }

        String right = combined.substring(delimiterPos + DELIMITER.length());
        return !right.isBlank() ? right : null;
    }
}
