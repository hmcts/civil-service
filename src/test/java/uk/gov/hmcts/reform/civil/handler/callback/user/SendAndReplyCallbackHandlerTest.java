package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.callback.TaskCompletionSubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageReply;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContext;
import uk.gov.hmcts.reform.civil.model.taskmanagement.Task;
import uk.gov.hmcts.reform.civil.model.taskmanagement.UserTask;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SendAndReplyMessageService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.taskmanagement.WaTaskManagementService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class SendAndReplyCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String AUTH_TOKEN = "BEARER_TOKEN";
    private static final Long CASE_ID = 1L;
    private static final String TASK_ID = "task-id";
    private static final String USER_ID = "user-id";

    @Mock
    SendAndReplyMessageService messageService;

    @Mock
    WaTaskManagementService taskManagementService;

    @Mock
    UserService userService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SendAndReplyCallbackHandler handler;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void  setup() {
        handler = new SendAndReplyCallbackHandler(messageService, new ObjectMapper(), userService, featureToggleService, taskManagementService);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_AND_REPLY);
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldClearSendAndReplyOption_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getSendAndReplyOption()).isNull();
        }

        @Test
        void shouldSetTheNotificationSendAndReplyOption_WhenAboutToStartIsInvokedFlagEnabled() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setClaimantBilingualLanguagePreference("BOTH");
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getBilingualHint()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldSetTheNotificationSendAndReplyOptionRespondentBiligual_WhenAboutToStartIsInvokedFlagEnabled() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setClaimantBilingualLanguagePreference("ENGLISH");
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            caseData.setCaseDataLiP(caseDataLiP);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getBilingualHint()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldPopulateMessagesToReplyTo_whenMessagesExist() {
            List<Element<Message>> messages = List.of(element(new Message()));
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setMessages(messages);
            DynamicList expectedMessages = DynamicList.fromList(List.of("mock"));
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
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
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            handler.handle(params);

            verifyNoInteractions(messageService);
        }

    }

    @Nested
    class PopulateMessageHistory {

        @Test
        void shouldNotInteractWithMessageService_whenSendAndReplyOptionIsSend() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "populate-message-history");

            handler.handle(params);

            verifyNoInteractions(messageService);
        }

        @Test
        void shouldReturnExpectedMessageHistory() {
            DynamicList messagesToReplyTo = new DynamicList();
            messagesToReplyTo.setValue(DynamicListElement.dynamicElement("message"));
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(REPLY);
            caseData.setMessagesToReplyTo(messagesToReplyTo);
            caseData.setMessages(List.of());
            Element<Message> message = element(new Message());
            String expectedTableMarkup = "<table></table>";

            when(messageService.getMessageById(caseData.getMessages(), messagesToReplyTo.getValue().getCode()))
                .thenReturn(message);
            when(messageService.renderMessageTableList(message))
                .thenReturn(expectedTableMarkup);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "populate-message-history");

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

            SendMessageMetadata messageMetaData = new SendMessageMetadata();
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
            caseData.setSendMessageMetadata(messageMetaData);
            caseData.setSendMessageContent(messageContent);

            Message expectedMessage = new Message();
            expectedMessage.setMessageContent(messageContent);
            expectedMessage.setRecipientRoleType(RolePool.ADMIN);
            List<Message> expectedMessages = List.of(expectedMessage);
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

            SendMessageMetadata messageMetaData = new SendMessageMetadata();
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setResponseClaimTrack(AllocatedTrack.FAST_CLAIM.name());
            caseData.setSendMessageMetadata(messageMetaData);
            caseData.setSendMessageContent(messageContent);

            Message expectedMessage = new Message();
            expectedMessage.setMessageContent(messageContent);
            expectedMessage.setRecipientRoleType(RolePool.JUDICIAL);
            List<Message> expectedMessages = List.of(expectedMessage);
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
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(10_000_01));
            caseData.setClaimValue(claimValue);
            SendMessageMetadata messageMetaData = new SendMessageMetadata();
            caseData.setSendMessageMetadata(messageMetaData);
            String messageContent = "Message Content";
            caseData.setSendMessageContent(messageContent);

            Message expectedMessage = new Message();
            expectedMessage.setMessageContent(messageContent);
            expectedMessage.setRecipientRoleType(RolePool.JUDICIAL_CIRCUIT);
            List<Message> expectedMessages = List.of(expectedMessage);
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

            SendMessageMetadata messageMetaData = new SendMessageMetadata();
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(9999));
            caseData.setSendMessageMetadata(messageMetaData);
            caseData.setSendMessageContent(messageContent);

            Message expectedMessage = new Message();
            expectedMessage.setMessageContent(messageContent);
            expectedMessage.setRecipientRoleType(RolePool.JUDICIAL_DISTRICT);
            List<Message> expectedMessages = List.of(expectedMessage);
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
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_MessageIsSentToJudge_Intermediate_responseClaimTrack() {
            String messageContent = "Message Content";

            SendMessageMetadata messageMetaData = new SendMessageMetadata();
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setResponseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name());
            caseData.setSendMessageMetadata(messageMetaData);
            caseData.setSendMessageContent(messageContent);

            Message expectedMessage = new Message();
            expectedMessage.setMessageContent(messageContent);
            expectedMessage.setRecipientRoleType(RolePool.JUDICIAL);
            List<Message> expectedMessages = List.of(expectedMessage);
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
            assertThat(responseCaseData.getLastMessageAllocatedTrack()).isEqualTo("Intermediate track");
            assertThat(responseCaseData.getLastMessageJudgeLabel()).isEqualTo("Judge");

            verify(messageService, times(1))
                .addMessage(null, messageMetaData, messageContent, AUTH_TOKEN);
        }

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_MessageIsSentToJudge_Multi_responseClaimTrack() {
            String messageContent = "Message Content";

            SendMessageMetadata messageMetaData = new SendMessageMetadata();
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setResponseClaimTrack(AllocatedTrack.MULTI_CLAIM.name());
            caseData.setSendMessageMetadata(messageMetaData);
            caseData.setSendMessageContent(messageContent);

            Message expectedMessage = new Message();
            expectedMessage.setMessageContent(messageContent);
            expectedMessage.setRecipientRoleType(RolePool.JUDICIAL);
            List<Message> expectedMessages = List.of(expectedMessage);
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
            assertThat(responseCaseData.getLastMessageAllocatedTrack()).isEqualTo("Multi track");
            assertThat(responseCaseData.getLastMessageJudgeLabel()).isEqualTo("Judge");

            verify(messageService, times(1))
                .addMessage(null, messageMetaData, messageContent, AUTH_TOKEN);
        }

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_MessageIsSentToDistrictJudge_Intermediate_TotalClaimAmount() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

            SendMessageMetadata messageMetaData = new SendMessageMetadata();
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(25001));
            caseData.setSendMessageMetadata(messageMetaData);
            String messageContent = "Message Content";
            caseData.setSendMessageContent(messageContent);

            Message expectedMessage = new Message();
            expectedMessage.setMessageContent(messageContent);
            expectedMessage.setRecipientRoleType(RolePool.JUDICIAL_DISTRICT);
            List<Message> expectedMessages = List.of(expectedMessage);
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
            assertThat(responseCaseData.getLastMessageAllocatedTrack()).isEqualTo("Intermediate track");
            assertThat(responseCaseData.getLastMessageJudgeLabel()).isEqualTo("DJ");

            verify(messageService, times(1))
                .addMessage(null, messageMetaData, messageContent, AUTH_TOKEN);
        }

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_MessageIsSentToDistrictJudge_Multi_TotalClaimAmount() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

            SendMessageMetadata messageMetaData = new SendMessageMetadata();
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(SEND);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(100001));
            caseData.setSendMessageMetadata(messageMetaData);
            String messageContent = "Message Content";
            caseData.setSendMessageContent(messageContent);

            Message expectedMessage = new Message();
            expectedMessage.setMessageContent(messageContent);
            expectedMessage.setRecipientRoleType(RolePool.JUDICIAL_DISTRICT);
            List<Message> expectedMessages = List.of(expectedMessage);
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
            assertThat(responseCaseData.getLastMessageAllocatedTrack()).isEqualTo("Multi track");
            assertThat(responseCaseData.getLastMessageJudgeLabel()).isEqualTo("DJ");

            verify(messageService, times(1))
                .addMessage(null, messageMetaData, messageContent, AUTH_TOKEN);
        }

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_andSendAndReplyOptionIsReply() {
            Message expectedMessage = new Message();
            expectedMessage.setMessageContent("Original Message");
            Element<Message> message = element(expectedMessage);

            Message originalMessage = message.getValue();
            Message updatedMessage = new Message();
            updatedMessage.setSentTime(originalMessage.getSentTime());
            updatedMessage.setUpdatedTime(originalMessage.getUpdatedTime());
            updatedMessage.setSenderRoleType(originalMessage.getSenderRoleType());
            updatedMessage.setSenderName(originalMessage.getSenderName());
            updatedMessage.setRecipientRoleType(originalMessage.getRecipientRoleType());
            updatedMessage.setSubjectType(originalMessage.getSubjectType());
            updatedMessage.setSubject(originalMessage.getSubject());
            updatedMessage.setMessageContent(originalMessage.getMessageContent());
            updatedMessage.setIsUrgent(originalMessage.getIsUrgent());
            updatedMessage.setMessageId(originalMessage.getMessageId());
            updatedMessage.setHistory(List.of(element(messageService.buildReplyOutOfMessage(originalMessage))));
            Element<Message> updatedElement = new Element<>();
            updatedElement.setId(message.getId());
            updatedElement.setValue(updatedMessage);

            MessageReply messageReply = new MessageReply();
            messageReply.setMessageContent("Reply to message");
            List<Element<Message>> messages = List.of(message);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSendAndReplyOption(REPLY);
            caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
            caseData.setMessages(messages);
            DynamicListElement replyList = DynamicListElement.dynamicElement("mock");
            DynamicList dynamicList = new DynamicList();
            dynamicList.setValue(replyList);
            caseData.setMessagesToReplyTo(dynamicList);
            caseData.setMessageReplyMetadata(messageReply);
            caseData.setMessageHistory("message history markup");

            List<Element<Message>> updatedMessages = List.of(updatedElement);
            when(messageService.addReplyToMessage(messages, replyList.getCode(), messageReply, AUTH_TOKEN, caseData)).thenReturn(
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
                .addReplyToMessage(messages, replyList.getCode(), messageReply, AUTH_TOKEN, caseData);
        }
    }

    @Nested
    class Submitted {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithSendOption() {

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setSendAndReplyOption(SEND);
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
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithReplyOption_andNoTaskToComplete() {
            when(taskManagementService.getTaskToComplete(any(), any(), any())).thenReturn(null);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setSendAndReplyOption(REPLY);

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

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithReplyOption_andTaskToCompleteIsUnassigned() {
            Task task = new Task();
            task.setId(TASK_ID);
            task.setAssignee(USER_ID);
            task.setTaskTitle("My Task");
            task.setTaskState("unassigned");

            when(userService.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().id(USER_ID).build());
            when(taskManagementService.getTaskToComplete(eq(CASE_ID.toString()), eq(AUTH_TOKEN), any())).thenReturn(task);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setSendAndReplyOption(REPLY);

            UserTask userTask = new UserTask();
            userTask.setCompleteTask(true);
            userTask.setTaskData(task);
            ClientContext clientContext = new ClientContext();
            clientContext.setUserTask(userTask);
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            TaskCompletionSubmittedCallbackResponse response = (TaskCompletionSubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(
                TaskCompletionSubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# Reply sent")
                    .confirmationBody(
                        "<br /><h2 class=\"govuk-heading-m\">What happens next</h2><br />A task has been created to review your reply.")
                    .clientContext(clientContext)
                    .build());

            verify(taskManagementService).claimTask(AUTH_TOKEN, TASK_ID);
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithReplyOption_andTaskToCompleteIsAssigned() {
            Task task = new Task();
            task.setId(TASK_ID);
            task.setAssignee(USER_ID);
            task.setTaskTitle("My Task");
            task.setTaskState("assigned");

            when(taskManagementService.getTaskToComplete(any(), any(), any())).thenReturn(task);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(CASE_ID);
            caseData.setSendAndReplyOption(REPLY);

            UserTask userTask2 = new UserTask();
            userTask2.setCompleteTask(true);
            userTask2.setTaskData(task);
            ClientContext clientContext2 = new ClientContext();
            clientContext2.setUserTask(userTask2);
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            TaskCompletionSubmittedCallbackResponse response = (TaskCompletionSubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(
                TaskCompletionSubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# Reply sent")
                    .confirmationBody(
                        "<br /><h2 class=\"govuk-heading-m\">What happens next</h2><br />A task has been created to review your reply.")
                    .clientContext(clientContext2)
                    .build());
        }
    }
}
