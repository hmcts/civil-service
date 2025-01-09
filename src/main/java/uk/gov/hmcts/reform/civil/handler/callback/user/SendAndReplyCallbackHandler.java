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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SendAndReplyMessageService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_AND_REPLY;
import static uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption.REPLY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class SendAndReplyCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_AND_REPLY);
    private static final String SEND_MESSAGE_CONFIRMATION_HEADER = "# Your message has been sent";
    private static final String SEND_MESSAGE_BODY_CONFIRMATION = "<br /><h2 class=\"govuk-heading-m\">What happens next</h2>" +
        "<br />A task has been created to review your message.";
    private static final String REPLY_MESSAGE_CONFIRMATION_HEADER = "# Reply sent";
    private static final String REPLY_MESSAGE_BODY_CONFIRMATION = "<br /><h2 class=\"govuk-heading-m\">What happens next</h2>" +
        "<br />A task has been created to review your reply.";

    private final SendAndReplyMessageService messageService;

    private final ObjectMapper objectMapper;

    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::handleAboutToStart,
            callbackKey(MID, "populate-message-history"), this::populateMessageHistory,
            callbackKey(ABOUT_TO_SUBMIT), this::handleAboutToSubmit,
            callbackKey(SUBMITTED), this::handleSubmitted
        );
    }

    private CallbackResponse populateMessageHistory(CallbackParams params) {
        CaseData caseData = params.getCaseData();

        if (!REPLY.equals(caseData.getSendAndReplyOption())) {
            return emptyCallbackResponse(params);
        }

        Element<Message> messageToReplyTo = getMessageToReplyTo(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder()
                      .messageHistory(messageService.renderMessageTableList(messageToReplyTo))
                      .build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse handleAboutToStart(CallbackParams params) {
        CaseData caseData = params.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        if (nonNull(caseData.getMessages()) && !caseData.getMessages().isEmpty()) {
            builder.messagesToReplyTo(messageService.createMessageSelectionList(caseData.getMessages()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.sendAndReplyOption(null).build().toMap(objectMapper)).build();
    }

    private Element<Message> getMessageToReplyTo(CaseData caseData) {
        String messageId = caseData.getMessagesToReplyTo().getValue().getCode();
        return messageService.getMessageById(caseData.getMessages(), messageId);
    }

    private CallbackResponse handleAboutToSubmit(CallbackParams params) {
        CaseData caseData = params.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        String userAuth = params.getParams().get(BEARER_TOKEN).toString();

        List<Element<Message>> messagesNew;

        if (SendAndReplyOption.SEND.equals(caseData.getSendAndReplyOption())) {
            messagesNew = messageService.addMessage(
                caseData.getMessages(),
                caseData.getSendMessageMetadata(),
                caseData.getSendMessageContent(),
                userAuth
            );

            builder.messages(messagesNew)
                .sendMessageMetadata(null)
                .sendMessageContent(null);
        } else {
            messagesNew = messageService.addReplyToMessage(
                caseData.getMessages(),
                caseData.getMessagesToReplyTo().getValue().getCode(),
                caseData.getMessageReplyMetadata(),
                userAuth
            );
            builder.messages(messagesNew)
                .messagesToReplyTo(null)
                .messageReplyMetadata(null)
                .messageHistory(null);
        }

        Element<Message> lastMessageElement = messagesNew.stream()
                .max(Comparator.comparing(message -> message.getValue().getUpdatedTime()))
                .orElse(element(Message.builder().build()));
        Message lastMessage = lastMessageElement.getValue();

        builder.lastMessage(lastMessage);

        AllocatedTrack allocatedTrack;
        if (Objects.nonNull(caseData.getAssignedTrackType())) {
            allocatedTrack = caseData.getAssignedTrackType();
        } else {
            allocatedTrack = AllocatedTrack.getAllocatedTrack(
                caseData.getClaimAmountInPounds(),
                caseData.getClaimType(),
                caseData.getPersonalInjuryType(),
                featureToggleService,
                caseData
            );
        }
        builder.lastMessageAllocatedTrack(AllocatedTrack.toStringValueForMessage(allocatedTrack));

        boolean isRecipientCircuitJudge = RolePool.JUDICIAL_CIRCUIT.equals(lastMessage.getRecipientRoleType());
        boolean isRecipientDistrictJudge = RolePool.JUDICIAL_DISTRICT.equals(lastMessage.getRecipientRoleType());
        boolean isJudge = RolePool.JUDICIAL.equals(lastMessage.getRecipientRoleType());

        if (isRecipientCircuitJudge || isRecipientDistrictJudge) {
            builder.lastMessageJudgeLabel(isRecipientCircuitJudge ? "CJ" : "DJ");
        } else if (isJudge) {
            builder.lastMessageJudgeLabel("Judge");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper)).build();
    }

    private CallbackResponse handleSubmitted(CallbackParams params) {
        if (SendAndReplyOption.SEND.equals(params.getCaseData().getSendAndReplyOption())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(SEND_MESSAGE_CONFIRMATION_HEADER)
                .confirmationBody(SEND_MESSAGE_BODY_CONFIRMATION)
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(REPLY_MESSAGE_CONFIRMATION_HEADER)
                .confirmationBody(REPLY_MESSAGE_BODY_CONFIRMATION)
                .build();
        }
    }
}
