package uk.gov.hmcts.reform.civil.notify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notify.audit.NotificationAuditService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private NotificationAuditService notificationAuditService;

    private NotificationService service;

    private NotificationService serviceWithAudit() {
        return new NotificationService(notificationClient, Optional.of(notificationAuditService));
    }

    private NotificationService serviceWithoutAudit() {
        return new NotificationService(notificationClient, Optional.empty());
    }

    @Test
    void shouldCallNotificationClient_whenRequestsSendEmail() throws NotificationClientException {
        // Given
        service = serviceWithoutAudit();
        SendEmailResponse response = mock(SendEmailResponse.class);
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(response);
        given(response.getNotificationId()).willReturn(UUID.randomUUID());

        // When
        service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference");
        // Then
        verify(notificationClient)
            .sendEmail("template", "email@email.com", Map.of("param1", "param1"), "reference");
    }

    @Test
    void shouldNotThrowException_whenSendEmailResponseIsNull() throws NotificationClientException {
        // Given
        service = serviceWithoutAudit();
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(null);

        // When
        service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference");

        // Then
        verify(notificationClient)
            .sendEmail("template", "email@email.com", Map.of("param1", "param1"), "reference");
    }

    @Test
    void shouldNotThrowException_whenSendEmailResponseNotificationIdIsNull() throws NotificationClientException {
        // Given
        service = serviceWithoutAudit();
        SendEmailResponse response = mock(SendEmailResponse.class);
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(response);
        given(response.getNotificationId()).willReturn(null);

        // When
        service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference");

        // Then
        verify(notificationClient)
            .sendEmail("template", "email@email.com", Map.of("param1", "param1"), "reference");
    }

    @Test
    void shouldReturnException_whenNotificationClientSendEmailReturnsException() throws NotificationClientException {
        // Given
        service = serviceWithoutAudit();
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
        service = serviceWithoutAudit();
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
        service = serviceWithoutAudit();
        given(notificationClient.sendLetter(any(), any(), any())).willThrow(new NotificationClientException("error"));
        // When  // Then
        NotificationException exception = assertThrows(
            NotificationException.class, () ->
                service.sendLetter("template", Map.of("param1", "param1"), "reference"));
        assertNotNull(exception.getMessage());
    }

    @Test
    void shouldRecordAudit_whenAuditServicePresentAndSendSucceeds() throws NotificationClientException {
        // Given
        service = serviceWithAudit();
        SendEmailResponse response = mock(SendEmailResponse.class);
        UUID notificationId = UUID.randomUUID();
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(response);
        given(response.getNotificationId()).willReturn(notificationId);

        // When
        service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference");

        // Then
        verify(notificationAuditService).record(
            eq("template"),
            eq("email@email.com"),
            eq("reference"),
            eq(notificationId.toString())
        );
    }

    @Test
    void shouldNotRecordAudit_whenSendThrows() throws NotificationClientException {
        // Given
        service = serviceWithAudit();
        given(notificationClient.sendEmail(any(), any(), any(), any()))
            .willThrow(new NotificationClientException("boom"));

        // When  // Then
        assertThrows(NotificationException.class, () ->
            service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference"));
        verify(notificationAuditService, never()).record(any(), any(), any(), any());
    }

    @Test
    void shouldNotPropagate_whenAuditServiceThrows() throws NotificationClientException {
        // Given
        service = serviceWithAudit();
        SendEmailResponse response = mock(SendEmailResponse.class);
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(response);
        given(response.getNotificationId()).willReturn(UUID.randomUUID());
        doThrow(new RuntimeException("audit broken"))
            .when(notificationAuditService).record(any(), any(), any(), any());

        // When  // Then — must not throw
        service.sendMail("email@email.com", "template", Map.of("param1", "param1"), "reference");
        verify(notificationClient)
            .sendEmail("template", "email@email.com", Map.of("param1", "param1"), "reference");
    }
}
