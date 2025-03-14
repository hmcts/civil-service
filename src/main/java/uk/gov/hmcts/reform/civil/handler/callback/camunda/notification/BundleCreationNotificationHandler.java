package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notification.handlers.BundleCreationNotifier;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_BUNDLE_CREATION;

@Service
@RequiredArgsConstructor
public class BundleCreationNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_BUNDLE_CREATION);
    public static final String TASK_ID = "BundleCreationNotifier";
    private final BundleCreationNotifier bundleCreationNotifier;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyForBundleCreation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyForBundleCreation(CallbackParams callbackParams) {
        bundleCreationNotifier.notifyParties(callbackParams.getCaseData(), NOTIFY_BUNDLE_CREATION.toString(), TASK_ID);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
