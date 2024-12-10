package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageReply;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SendAndReplyMessageService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_AND_REPLY;
import static uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption.REPLY;
import static uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption.SEND;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class SendAndReplyCallbackHandlerTest {

    private static final String AUTH_TOKEN = "BEARER_TOKEN";

    @Mock
    SendAndReplyMessageService messageService;
    @InjectMocks
    private SendAndReplyCallbackHandler handler;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void  setup() {
        handler = new SendAndReplyCallbackHandler(messageService, new ObjectMapper(), featureToggleService);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_AND_REPLY);
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldClearSendAndReplyOption_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseData.builder()
                .sendAndReplyOption(SEND)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getSendAndReplyOption()).isNull();
        }

        @Test
        void shouldPopulateMessagesToReplyTo_whenMessagesExist() {
            List<Element<Message>> messages = List.of(element(Message.builder().build()));
            CaseData caseData = CaseData.builder()
                .messages(messages)
                .build();
            DynamicList expectedMessages = DynamicList.fromList(List.of("mock"));

            when(messageService.createMessageSelectionList(messages)).thenReturn(expectedMessages);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getMessagesToReplyTo()).isEqualTo(expectedMessages);

            verify(messageService, times(1)).createMessageSelectionList(messages);
        }

        @Test
        void shouldNotInteractWithMessagesService_whenNoMessagesExist() {
            CaseData caseData = CaseData.builder().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            handler.handle(params);

            verifyNoInteractions(messageService);
        }

    }

    @Nested
    class PopulateMessageHistory {

        @Test
        void shouldNotInteractWithMessageService_whenSendAndReplyOptionIsSend() {
            CaseData caseData = CaseData.builder()
                .sendAndReplyOption(SEND)
                .build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.MID)
                .pageId("populate-message-history")
                .build();

            handler.handle(params);

            verifyNoInteractions(messageService);
        }

        @Test
        void shouldReturnExpectedMessageHistory() {
            DynamicList messagesToReplyTo = DynamicList.builder().value(
                DynamicListElement.dynamicElement("message")).build();
            CaseData caseData = CaseData.builder()
                .sendAndReplyOption(REPLY)
                .messagesToReplyTo(messagesToReplyTo)
                .messages(List.of())
                .build();
            Element<Message> message = element(Message.builder().build());
            String expectedTableMarkup = "<table></table>";

            when(messageService.getMessageById(caseData.getMessages(), messagesToReplyTo.getValue().getCode()))
                .thenReturn(message);
            when(messageService.renderMessageTableList(message))
                .thenReturn(expectedTableMarkup);

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.MID)
                .pageId("populate-message-history")
                .build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getMessageHistory()).isEqualTo(expectedTableMarkup);

            verify(messageService, times(1))
                .getMessageById(caseData.getMessages(), messagesToReplyTo.getValue().getCode());
            verify(messageService, times(1))
                .renderMessageTableList(message);
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_MessageIsSentToNonJudge_Small_Claim_allocatedTrack() {
            String messageContent = "Message Content";
            SendMessageMetadata messageMetaData = SendMessageMetadata.builder().build();

            Message expectedMessage = Message.builder()
                .messageContent(messageContent)
                .recipientRoleType(RolePool.ADMIN)
                .build();
            List<Message> expectedMessages = List.of(expectedMessage);

            CaseData caseData = CaseData.builder()
                .sendAndReplyOption(SEND)
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .sendMessageMetadata(messageMetaData)
                .sendMessageContent(messageContent)
                .build();

            when(messageService.addMessage(null, messageMetaData, messageContent, AUTH_TOKEN))
                .thenReturn(wrapElements(expectedMessages));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            assertThat(responseCaseData.getSendMessageMetadata()).isNull();
            assertThat(responseCaseData.getSendMessageContent()).isNull();
            assertThat(unwrapElements(responseCaseData.getMessages())).isEqualTo(expectedMessages);
            assertThat(responseCaseData.getLastMessage()).isEqualTo(expectedMessage);
            assertThat(responseCaseData.getLastMessageAllocatedTrack()).isEqualTo("Small claim");
            assertThat(responseCaseData.getLastMessageJudgeLabel()).isNull();

            verify(messageService, times(1))
                .addMessage(null, messageMetaData, messageContent, AUTH_TOKEN);
        }

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_MessageIsSentToJudge_Fasttrack_responseClaimTrack() {
            String messageContent = "Message Content";
            SendMessageMetadata messageMetaData = SendMessageMetadata.builder().build();

            Message expectedMessage = Message.builder()
                .messageContent(messageContent)
                .recipientRoleType(RolePool.JUDICIAL)
                .build();
            List<Message> expectedMessages = List.of(expectedMessage);

            CaseData caseData = CaseData.builder()
                .sendAndReplyOption(SEND)
                .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
                .sendMessageMetadata(messageMetaData)
                .sendMessageContent(messageContent)
                .build();

            when(messageService.addMessage(null, messageMetaData, messageContent, AUTH_TOKEN))
                .thenReturn(wrapElements(expectedMessages));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            assertThat(responseCaseData.getSendMessageMetadata()).isNull();
            assertThat(responseCaseData.getSendMessageContent()).isNull();
            assertThat(unwrapElements(responseCaseData.getMessages())).isEqualTo(expectedMessages);
            assertThat(responseCaseData.getLastMessage()).isEqualTo(expectedMessage);
            assertThat(responseCaseData.getLastMessageAllocatedTrack()).isEqualTo("Fast track");
            assertThat(responseCaseData.getLastMessageJudgeLabel()).isEqualTo("Judge");

            verify(messageService, times(1))
                .addMessage(null, messageMetaData, messageContent, AUTH_TOKEN);
        }

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_MessageIsSentToCircuitJudge_Fast_Track_ClaimValue() {
            String messageContent = "Message Content";
            SendMessageMetadata messageMetaData = SendMessageMetadata.builder().build();

            Message expectedMessage = Message.builder()
                .messageContent(messageContent)
                .recipientRoleType(RolePool.JUDICIAL_CIRCUIT)
                .build();
            List<Message> expectedMessages = List.of(expectedMessage);

            CaseData caseData = CaseData.builder()
                .sendAndReplyOption(SEND)
                .claimValue(ClaimValue.builder().statementOfValueInPennies(BigDecimal.valueOf(1000001)).build())
                .sendMessageMetadata(messageMetaData)
                .sendMessageContent(messageContent)
                .build();

            when(messageService.addMessage(null, messageMetaData, messageContent, AUTH_TOKEN))
                .thenReturn(wrapElements(expectedMessages));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            assertThat(responseCaseData.getSendMessageMetadata()).isNull();
            assertThat(responseCaseData.getSendMessageContent()).isNull();
            assertThat(unwrapElements(responseCaseData.getMessages())).isEqualTo(expectedMessages);
            assertThat(responseCaseData.getLastMessage()).isEqualTo(expectedMessage);
            assertThat(responseCaseData.getLastMessageAllocatedTrack()).isEqualTo("Fast track");
            assertThat(responseCaseData.getLastMessageJudgeLabel()).isEqualTo("CJ");

            verify(messageService, times(1))
                .addMessage(null, messageMetaData, messageContent, AUTH_TOKEN);
        }

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_MessageIsSentToDistrictJudge_SmallClaim_TotalClaimAmount() {
            String messageContent = "Message Content";
            SendMessageMetadata messageMetaData = SendMessageMetadata.builder().build();

            Message expectedMessage = Message.builder()
                .messageContent(messageContent)
                .recipientRoleType(RolePool.JUDICIAL_DISTRICT)
                .build();
            List<Message> expectedMessages = List.of(expectedMessage);

            CaseData caseData = CaseData.builder()
                .sendAndReplyOption(SEND)
                .totalClaimAmount(BigDecimal.valueOf(9999))
                .sendMessageMetadata(messageMetaData)
                .sendMessageContent(messageContent)
                .build();

            when(messageService.addMessage(null, messageMetaData, messageContent, AUTH_TOKEN))
                .thenReturn(wrapElements(expectedMessages));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            assertThat(responseCaseData.getSendMessageMetadata()).isNull();
            assertThat(responseCaseData.getSendMessageContent()).isNull();
            assertThat(unwrapElements(responseCaseData.getMessages())).isEqualTo(expectedMessages);
            assertThat(responseCaseData.getLastMessage()).isEqualTo(expectedMessage);
            assertThat(responseCaseData.getLastMessageAllocatedTrack()).isEqualTo("Small claim");
            assertThat(responseCaseData.getLastMessageJudgeLabel()).isEqualTo("DJ");

            verify(messageService, times(1))
                .addMessage(null, messageMetaData, messageContent, AUTH_TOKEN);
        }

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_andSendAndReplyOptionIsReply() {
            Element<Message> message = element(Message.builder().messageContent("Original Message").build());
            MessageReply messageReply = MessageReply.builder().messageContent("Reply to message").build();
            List<Element<Message>> messages = List.of(message);

            List<Element<Message>> updatedMessages = List.of(
                Element.<Message>builder()
                    .id(message.getId())
                    .value(
                        message.getValue().toBuilder()
                            .history(List.of(element(messageService.buildReplyOutOfMessage(message.getValue()))))
                            .build())
                    .build());

            DynamicListElement replyList = DynamicListElement.dynamicElement("mock");

            CaseData caseData = CaseData.builder()
                .sendAndReplyOption(REPLY)
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .messages(messages)
                .messagesToReplyTo(DynamicList.builder().value(replyList).build())
                .messageReplyMetadata(messageReply)
                .messageHistory("message history markup")
                .build();

            when(messageService.addReplyToMessage(messages, replyList.getCode(), messageReply, AUTH_TOKEN)).thenReturn(
                updatedMessages);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            assertThat(responseCaseData.getMessageReplyMetadata()).isNull();
            assertThat(responseCaseData.getMessageHistory()).isNull();
            assertThat(responseCaseData.getMessagesToReplyTo()).isNull();
            assertThat(responseCaseData.getMessages()).isEqualTo(updatedMessages);

            verify(messageService, times(1))
                .addReplyToMessage(messages, replyList.getCode(), messageReply, AUTH_TOKEN);
        }
    }

    @Nested
    class Submitted {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithLiftStayForSendingMessage() {

            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .sendAndReplyOption(SEND).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# Your message has been sent")
                    .confirmationBody(
                        "<br /><h2 class=\"govuk-heading-m\">What happens next</h2><br />A task has been created to review your message.")
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithLiftStayForReplyingToMessage() {

            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .sendAndReplyOption(REPLY).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# Reply sent")
                    .confirmationBody(
                        "<br /><h2 class=\"govuk-heading-m\">What happens next</h2><br />A task has been created to review your reply.")
                    .build());
        }
    }
}
