package uk.gov.hmcts.reform.unspec.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import lombok.SneakyThrows;
import org.apache.http.HttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SendGridClient.class})
class SendGridClientTest {

    private static final Response SUCCESSFUL_RESPONSE = new Response(
        202,
        "response body",
        Map.of()
    );
    private static final EmailData EMAIL_DATA = EmailData.builder()
        .to("to@server.net")
        .subject("my email")
        .message("My email message")
        .attachments(List.of())
        .build();
    private static final String EMAIL_FROM = "from@server.net";

    @MockBean
    private SendGrid sendGrid;

    @Captor
    private ArgumentCaptor<Request> requestCaptor;

    @Autowired
    private SendGridClient sendGridClient;

    @Nested
    class InvalidDataProvided {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowIllegalArgumentException_whenFromIsNullOrBlank(String from) {
            assertThrows(
                IllegalArgumentException.class,
                () -> sendGridClient.sendEmail(from, EMAIL_DATA)
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowIllegalArgumentException_whenToIsNullOrBlank(String to) {
            EmailData emailData = EmailData.builder()
                .to(to)
                .subject("my email")
                .message("My email message")
                .attachments(List.of())
                .build();
            assertThrows(
                IllegalArgumentException.class,
                () -> sendGridClient.sendEmail(EMAIL_FROM, emailData)
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowIllegalArgumentException_whenSubjectIsNullOrBlank(String subject) {
            EmailData emailData = EmailData.builder()
                .to("to@server.net")
                .subject(subject)
                .message("My email message")
                .attachments(List.of())
                .build();
            assertThrows(
                IllegalArgumentException.class,
                () -> sendGridClient.sendEmail(EMAIL_FROM, emailData)
            );
        }

        @Test
        void shouldThrowIllegalArgumentException_whenEmailIsNull() {
            assertThrows(
                IllegalArgumentException.class,
                () -> sendGridClient.sendEmail(EMAIL_FROM, null)
            );
        }
    }

    @Nested
    class SuccessfulSendEmail {

        @BeforeEach
        void setup() {
            clearInvocations(sendGrid);
        }

        @Test
        @SneakyThrows
        void shouldSendEmail_whenValidArgumentsProvided() {
            when(sendGrid.api(any(Request.class))).thenReturn(SUCCESSFUL_RESPONSE);

            sendGridClient.sendEmail(EMAIL_FROM, EMAIL_DATA);

            verify(sendGrid).api(requestCaptor.capture());
            Request capturedRequest = requestCaptor.getValue();
            assertEquals(Method.POST, capturedRequest.getMethod());
            assertEquals("mail/send", capturedRequest.getEndpoint());
            assertTrue(capturedRequest.getBody().contains("\"email\":\"" + EMAIL_FROM + "\""));
        }

        @Test
        @SneakyThrows
        void shouldSendEmail_whenEmptyMessageIsProvided() {
            when(sendGrid.api(any(Request.class))).thenReturn(SUCCESSFUL_RESPONSE);

            sendGridClient.sendEmail(EMAIL_FROM, EmailData.builder()
                .to("to@server.net")
                .subject("subject")
                .message("")
                .attachments(List.of())
                .build());

            verify(sendGrid).api(requestCaptor.capture());
            Request capturedRequest = requestCaptor.getValue();
            assertTrue(capturedRequest.getBody().contains("\"content\":[{\"type\":\"text/plain\",\"value\":\" \"}]"));
        }

        @Test
        @SneakyThrows
        void shouldSendEmail_whenAttachmentIsProvided() {
            when(sendGrid.api(any(Request.class))).thenReturn(SUCCESSFUL_RESPONSE);

            sendGridClient.sendEmail(EMAIL_FROM, EmailData.builder()
                .to("to@server.net")
                .subject("subject")
                .message("message")
                .attachments(List.of(EmailAttachment.pdf(new byte[]{1, 2, 3}, "test.pdf")))
                .build());

            verify(sendGrid).api(requestCaptor.capture());
            Request capturedRequest = requestCaptor.getValue();
            assertTrue(capturedRequest.getBody().contains("\"filename\":\"test.pdf\""));
        }
    }

    @Nested
    class FailureResponseFromSendGrid {

        @Test
        @SneakyThrows
        void shouldThrowEmailSendFailedException_whenSendGridThrowsIOException() {
            when(sendGrid.api(any(Request.class))).thenThrow(new IOException("expected exception"));
            assertThrows(
                EmailSendFailedException.class,
                () -> sendGridClient.sendEmail(EMAIL_FROM, EMAIL_DATA)
            );
        }

        @Test
        @SneakyThrows
        void shouldThrowEmailSendFailedException_whenSendGridThrows400Response() {
            when(sendGrid.api(any(Request.class)))
                .thenReturn(new Response(400, "bad request", Map.of()));

            EmailSendFailedException emailSendFailedException = assertThrows(
                EmailSendFailedException.class,
                () -> sendGridClient.sendEmail(EMAIL_FROM, EMAIL_DATA)
            );

            Throwable causeThrowable = emailSendFailedException.getCause();
            assertTrue(causeThrowable instanceof HttpException);
            HttpException cause = (HttpException) causeThrowable;
            assertEquals(
                "SendGrid returned a non-success response (400); body: bad request",
                cause.getMessage()
            );
        }
    }
}
