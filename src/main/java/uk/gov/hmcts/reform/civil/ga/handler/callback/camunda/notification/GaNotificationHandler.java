package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.GaNotifierFactory;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;

/**
 * The General Application entry point into the Notifier framework, mirroring the Civil
 * {@code NotificationHandler}.
 *
 * <p>Implementing {@link GeneralApplicationCallbackHandler} registers this under the
 * {@code GENERALAPPLICATION-NOTIFY_EVENT} key, so it does not collide with the Civil handler for
 * the same event. {@code CaseTypeHandlerKeyFactory} resolves which one runs from the incoming case
 * type.
 *
 * <p>This covers the sending half only. The second CCD event that records the summary against the
 * case, which is what the GA history tab needs, is deliberately not here: it requires a GA
 * {@code RECORD_NOTIFICATIONS} event and a {@code notificationSummary} field on GA case data, both
 * of which belong with DTSCCI-4296.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GaNotificationHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT);

    private final GaNotifierFactory gaNotifierFactory;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotifications);
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

        notifier.notifyParties(caseData, NOTIFY_EVENT.toString(), taskId);

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
