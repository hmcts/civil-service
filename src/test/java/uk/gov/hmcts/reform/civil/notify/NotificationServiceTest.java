package uk.gov.hmcts.reform.civil.notify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService service;

    @Mock
    private NotificationClient notificationClient;

    @Test
    void shouldCallNotificationClient_whenRequestsSendEmail() throws NotificationClientException {
        // Given
        given(notificationClient.sendEmail(any(), any(), any(), any())).willCallRealMethod();
        // When
        service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference");
        // Then
        verify(notificationClient)
            .sendEmail("template", "email@email.com", Map.of("param1", "param1"), "reference");
    }

    @Test
    void shouldReturnException_whenNotificationClientSendEmailReturnsException() throws NotificationClientException {
        // Given
        given(notificationClient.sendEmail(any(), any(), any(), any()))
            .willThrow(new NotificationClientException("error"));
        // When  // Then
        NotificationException exception = assertThrows(
            NotificationException.class, () ->
                service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference"));
        assertNotNull(exception.getMessage());
    }

    @Test
    void shouldCallNotificationClient_whenRequestsSendLetter() throws NotificationClientException {
        // Given
        given(notificationClient.sendLetter(any(), any(), any())).willAnswer(invocation -> null);
        // When
        service.sendLetter("template", Map.of("param1", "param1"), "reference");
        // Then
        verify(notificationClient)
            .sendLetter("template", Map.of("param1", "param1"), "reference");
    }

    @Test
    void shouldReturnException_whenNotificationClientSendLetterReturnsException() throws NotificationClientException {
        // Given
        given(notificationClient.sendLetter(any(), any(), any())).willThrow(new NotificationClientException("error"));
        // When  // Then
        NotificationException exception = assertThrows(
            NotificationException.class, () ->
                service.sendLetter("template", Map.of("param1", "param1"), "reference"));
        assertNotNull(exception.getMessage());
    }
}
