package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.GaNotifierFactory;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.notification.handlers.NotificationSummaryParser;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_NOTIFICATIONS;

/**
 * The General Application entry point into the Notifier framework, mirroring the Civil
 * {@code NotificationHandler}.
 *
 * <p>Implementing {@link GeneralApplicationCallbackHandler} registers this under the
 * {@code GENERALAPPLICATION-NOTIFY_EVENT} key, so it does not collide with the Civil handler for
 * the same event. {@code CaseTypeHandlerKeyFactory} resolves which one runs from the incoming case
 * type.
 *
 * <p>Both halves of the Civil behaviour are here. The about to submit callback sends the emails and
 * keeps the notifier's summary on the case; the submitted callback raises
 * {@code RECORD_NOTIFICATIONS}, which is the Record notification details entry the GA history tab
 * shows, with the recipients in its Summary and any errors in its Description. That is DTSCCI-4296,
 * and it mirrors {@code NotificationHandler} rather than reimplementing it: both parse the same
 * string through {@code NotificationSummaryParser}, because both notify through the same
 * {@code BaseNotifier}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GaNotificationHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT);

    private final GaNotifierFactory gaNotifierFactory;
    private final GaCoreCaseDataService gaCoreCaseDataService;
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
        return callbackParams.getGeneralApplicationCaseData().getBusinessProcess().getActivityId();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendNotifications(CallbackParams callbackParams) {
        final GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        final String taskId = caseData.getBusinessProcess().getActivityId();
        final Notifier<GeneralApplicationCaseData> notifier = gaNotifierFactory.getNotifier(taskId);

        if (notifier == null) {
            throw new IllegalStateException(
                "No GA Notifier registered for taskId '" + taskId + "' (caseId: "
                    + caseData.getCcdCaseReference() + "). "
                    + "Check the BPMN caseEvent/activityId and Notifier#getTaskId mappings.");
        }

        final String summary = notifier.notifyParties(caseData, NOTIFY_EVENT.toString(), taskId);

        caseData.setNotificationSummary(summary);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse recordNotifications(CallbackParams callbackParams) {
        final GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        final String caseId = caseData.getCcdCaseReference().toString();

        // the Ga variants, because these carry GENERALAPPLICATION_CASE_TYPE. startUpdate and
        // submitUpdate pass CASE_TYPE (CIVIL), which is what every other GA task handler avoids.
        StartEventResponse startEventResponse = gaCoreCaseDataService.startGaUpdate(caseId, RECORD_NOTIFICATIONS);
        gaCoreCaseDataService.submitGaUpdate(
            caseId, getCaseContent(startEventResponse, caseData.getNotificationSummary()));

        return SubmittedCallbackResponse.builder().build();
    }

    private CaseDataContent getCaseContent(StartEventResponse startEventResponse, String fullSummary) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();

        // cleared so the summary lives on the history event rather than on the case
        data.put("notificationSummary", null);

        Event.EventBuilder eventBuilder = Event.builder()
            .id(startEventResponse.getEventId())
            .summary(NotificationSummaryParser.attempted(fullSummary));

        String errors = NotificationSummaryParser.errors(fullSummary);
        if (errors != null && !errors.isEmpty()) {
            eventBuilder.description("Errors: " + errors);
        }

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(eventBuilder.build())
            .data(data)
            .build();
    }
}
