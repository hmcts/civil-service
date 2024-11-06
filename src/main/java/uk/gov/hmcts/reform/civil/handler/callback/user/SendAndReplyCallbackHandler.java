package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.SendAndReplyMessageService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.user.UserInformationService;
import uk.gov.hmcts.reform.civil.utils.UserRoleUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_AND_REPLY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class SendAndReplyCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_AND_REPLY);
    private static final String SEND_MESSAGE_CONFIRMATION_HEADER = "# Your message has been sent";
    private static final String BODY_CONFIRMATION = "<br /><h2 class=\"govuk-heading-m\">What happens next</h2>" +
        "<br />A task has been created to review your message.";

    private final SendAndReplyMessageService messageService;

    private final ObjectMapper objectMapper;

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
        CaseData caseData = params.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        if (SendAndReplyOption.SEND.equals(caseData.getSendAndReplyOption())) {
            String userAuth = params.getParams().get(BEARER_TOKEN).toString();
            builder.messages(messageService.addMessage(
                caseData.getMessages(),
                caseData.getSendMessageMetadata(),
                caseData.getSendMessageContent(),
                userAuth
            ));
        }

        builder.sendMessageMetadata(null)
            .sendMessageContent(null)
            .sendAndReplyOption(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper)).build();
    }

    private CallbackResponse handleSubmitted(CallbackParams params) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(SEND_MESSAGE_CONFIRMATION_HEADER)
            .confirmationBody(BODY_CONFIRMATION)
            .build();
    }
}
