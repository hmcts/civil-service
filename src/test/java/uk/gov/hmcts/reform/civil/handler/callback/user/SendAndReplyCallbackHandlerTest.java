package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.SendAndReplyMessageService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_AND_REPLY;

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

}
