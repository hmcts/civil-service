package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
//import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
//import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
//import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
//import uk.gov.hmcts.reform.civil.callback.CallbackParams;
//import uk.gov.hmcts.reform.civil.model.CaseData;
//import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
//import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
//import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
//import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
//import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.SendAndReplyMessageService;

//import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
//import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
//import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_AND_REPLY;
//import static uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption.REPLY;
//import static uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption.SEND;
//import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
//import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class SendAndReplyCallbackHandlerTest {

    private static String AUTH = "BEARER_TOKEN";

    @Mock
    SendAndReplyMessageService messageService;
    @InjectMocks
    private SendAndReplyCallbackHandler handler;

    @BeforeEach
    public void setup() {
        handler = new SendAndReplyCallbackHandler(messageService, new ObjectMapper());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_AND_REPLY);
    }

//    @Nested
//    class AboutToStart {
//
//        @Test
//        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
//            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateDecisionOutcome().build();
//            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();
//
//            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
//                .handle(params);
//
//            assertThat(response.getErrors()).isNull();
//        }
//    }

//    @Nested
//    class AboutToSubmit {
//
//        @Test
//        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked_andSendAndReplyOptionIsSend() {
//            String messageContent = "Message Content";
//            SendMessageMetadata messageMetaData = SendMessageMetadata.builder().build();
//
//            List<Message> expectedMessages = List.of(Message.builder().messageContent(messageContent).build());
//
//            CaseData caseData = CaseData.builder()
//                .sendAndReplyOption(SEND)
//                .sendMessageMetadata(messageMetaData)
//                .sendMessageContent(messageContent)
//                .build();
//
//            when(messageService.addMessage(null, messageMetaData, messageContent, AUTH))
//                .thenReturn(wrapElements(expectedMessages));
//
//            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
//
//            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
//                .handle(params);
//
//            CaseData responseCaseData = new ObjectMapper().convertValue(response.getData(), CaseData.class);
//
//            assertThat(response.getErrors()).isNull();
//            assertThat(responseCaseData.getSendAndReplyOption()).isNull();
//            assertThat(responseCaseData.getSendMessageMetadata()).isNull();
//            assertThat(responseCaseData.getSendMessageContent()).isNull();
//            assertThat(unwrapElements(responseCaseData.getMessages())).isEqualTo(expectedMessages);
//
//            verify(messageService, times(1))
//                .addMessage(eq(null), eq(messageMetaData), eq(messageContent), eq(AUTH));
//        }
//    }
//
//    @Test
//    void shouldNotReturnErrors_WhenAboutToSubmitIsInvoked_andSendAndReplyOptionIsReply() {
//        CaseData caseData = CaseData.builder()
//            .sendAndReplyOption(REPLY)
//            .build();
//
//        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
//
//        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
//            .handle(params);
//
//        assertThat(response.getErrors()).isNull();
//
//        verifyNoInteractions(messageService);
//    }
//
//    @Nested
//    class Submitted {
//
//        @Test
//        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedWithLiftStay() {
//
//            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
//                .manageStayOption("LIFT_STAY").build();
//            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
//            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
//
//            assertThat(response).usingRecursiveComparison().isEqualTo(
//                SubmittedCallbackResponse.builder()
//                    .confirmationHeader(
//                        "# Your message has been sent")
//                    .confirmationBody(
//                        "<br /><h2 class=\"govuk-heading-m\">What happens next</h2><br />A task has been created to review your message.")
//                    .build());
//        }
//    }
}
