package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    NotificationService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class NotificationServiceTest {

    @Autowired
    private NotificationService service;

    @MockBean
    private NotificationClient notificationClient;

    @Test
    void shouldCallNotificationClient_whenRequestsSendEmail() throws NotificationClientException {
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenCallRealMethod();
        service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference");
        verify(notificationClient)
            .sendEmail("template", "email@email.com", Map.of("param1", "param1"), "reference");
    }

    @Test
    void shouldReturnException_whenNotificationClientSendEmailReturnsException() throws NotificationClientException {
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException("error"));
        NotificationException exception = assertThrows(
            NotificationException.class, () ->
                service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference"));
        assertNotNull(exception.getMessage());
    }

    @Test
    void shouldCallNotificationClient_whenRequestsSendLetter() throws NotificationClientException {
        when(notificationClient.sendLetter(any(), any(), any())).thenAnswer(invocation -> null);
        service.sendLetter("template", Map.of("param1", "param1"), "reference");
        verify(notificationClient)
            .sendLetter("template", Map.of("param1", "param1"), "reference");
    }

    @Test
    void shouldReturnException_whenNotificationClientSendLetterReturnsException() throws NotificationClientException {
        when(notificationClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException("error"));
        NotificationException exception = assertThrows(
            NotificationException.class, () ->
                service.sendLetter("template", Map.of("param1", "param1"), "reference"));
        assertNotNull(exception.getMessage());
    }
}
