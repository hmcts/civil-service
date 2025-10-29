package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
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
@Slf4j
public class NotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT);

    private final NotifierFactory notifierFactory;
    private final CoreCaseDataService coreCaseDataService;
    private final uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService caseTaskTrackingService;

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
        notifier.notifyParties(caseData, NOTIFY_EVENT.toString(), taskId);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private CallbackResponse recordNotifications(CallbackParams callbackParams) {
        final CaseData caseData = callbackParams.getCaseData();
        final Long ref = caseData.getCcdCaseReference();
        if (ref == null) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        final String caseId = ref.toString();
        try {
            StartEventResponse startEvent = coreCaseDataService.startUpdate(caseId, RECORD_NOTIFICATIONS);

            Map<String, Object> data = startEvent.getCaseDetails().getData();

            String taskId = caseData.getBusinessProcess() != null
                ? caseData.getBusinessProcess().getActivityId()
                : null;
            String summary = taskId;
            String errors = taskId != null ? caseTaskTrackingService.consumeErrors(caseId, taskId) : null;

            CaseDataContent content = CaseDataContent.builder()
                .eventToken(startEvent.getToken())
                .event(Event.builder()
                           .id(startEvent.getEventId())
                           .summary(summary)
                           // Place raw error text into the event comments (description) without any prefix
                           .description(errors != null && !errors.isBlank() ? errors : null)
                           .build())
                .data(data)
                .build();

            coreCaseDataService.submitUpdate(caseId, content);
        } catch (Exception e) {
            log.warn("Record notifications event failed for case {}: {}", caseId, e.getMessage());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
