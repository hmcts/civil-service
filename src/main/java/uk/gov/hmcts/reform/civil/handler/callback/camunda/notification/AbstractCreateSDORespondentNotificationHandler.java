package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

/**
 * When an SDO is created it is notified to applicants and defendants.
 * This class holds common code for the defendant handlers
 */
@Service
@RequiredArgsConstructor
public abstract class AbstractCreateSDORespondentNotificationHandler extends CallbackHandler {

    /**
     * to be used when defendant is not represented.
     */
    private final AbstractCreateSDORespondentNotificationSender lipNotificationSender;
    /**
     * to be used when defendant is represented.
     */
    private final AbstractCreateSDORespondentNotificationSender lrNotificationSender;
    private final String taskId;
    private final List<CaseEvent> events;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorSDOTriggered
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return taskId;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return events;
    }

    protected abstract boolean isRespondentLiP(CaseData caseData);

    protected CallbackResponse notifyRespondentSolicitorSDOTriggered(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isRespondentLiP(caseData)) {
            lipNotificationSender.notifyRespondentPartySDOTriggered(caseData);
        } else {
            lrNotificationSender.notifyRespondentPartySDOTriggered(caseData);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}

