package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_NOTIFICATIONS;

@Service
@RequiredArgsConstructor
public class NotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT);
    private final NotifierFactory  notifierFactory;
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

        CaseData.CaseDataBuilder updatedCaseDataBuilder = caseData.toBuilder()
            .notificationSummary(summary);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse recordNotifications(CallbackParams callbackParams) {
        final CaseData caseData = callbackParams.getCaseData();
        final String caseId = caseData.getCcdCaseReference().toString();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, RECORD_NOTIFICATIONS);
        String summary = caseData.getNotificationSummary();
        CaseDataContent caseContent = getCaseContent(startEventResponse, summary);
        coreCaseDataService.submitUpdate(caseId, caseContent);
        return SubmittedCallbackResponse.builder().build();
    }

    private CaseDataContent getCaseContent(StartEventResponse startEventResponse, String summary) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .summary(summary)
                       .build())
            .data(data)
            .build();
    }
}
