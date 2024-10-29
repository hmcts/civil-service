package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_AND_REPLY;

@Service
@RequiredArgsConstructor
public class SendAndReplyCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_AND_REPLY);
    private static final String SEND_MESSAGE_CONFIRMATION_HEADER = "# Your message has been sent";
    private static final String BODY_CONFIRMATION = "<br /><h2 class=\"govuk-heading-m\">What happens next</h2>" +
        "<br />A task has been created to review your message.";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::handleAboutToSubmit,
            callbackKey(SUBMITTED), this::handleSubmitted
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse handleAboutToSubmit(CallbackParams params) {
        // ToDo: Create/AddTo captured message data to messages collection which is to be displayed in new messages tab.
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private CallbackResponse handleSubmitted(CallbackParams params) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(SEND_MESSAGE_CONFIRMATION_HEADER)
            .confirmationBody(BODY_CONFIRMATION)
            .build();
    }
}
