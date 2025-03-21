package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notification.handlers.AcknowledgeClaimNotifier;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_PARTIES_FOR_CLAIM_ACKNOWLEDGEMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcknowledgementClaimNotificationHandler extends CallbackHandler {

    private final AcknowledgeClaimNotifier acknowledgeClaimNotifier;

    private static final List<CaseEvent> EVENTS = List.of(
            NOTIFY_PARTIES_FOR_CLAIM_ACKNOWLEDGEMENT);

    public static final String TASK_ID = "AcknowledgeClaimNotifyParties";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::notifyPartiesForClaimAcknowledgement
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyPartiesForClaimAcknowledgement(CallbackParams callbackParams) {
        acknowledgeClaimNotifier.notifyParties(callbackParams.getCaseData());

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
