package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BaseNotifierTest {

    @Mock
    private NotificationService notificationService;

    private BaseNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notifier = new BaseNotifier(notificationService) {
        };
    }

    @Test
    void shouldSendNotificationsSuccessfully() {
        // Arrange
        EmailDTO email1 = EmailDTO.builder()
            .targetEmail("recipient1@example.com")
            .emailTemplate("template1")
            .parameters(Map.of("key1", "value1"))
            .reference("ref1")
            .build();

        EmailDTO email2 = EmailDTO.builder()
            .targetEmail("recipient2@example.com")
            .emailTemplate("template2")
            .parameters(Map.of("key2", "value2"))
            .reference("ref2")
            .build();

        Set<EmailDTO> recipients = Set.of(email1, email2);

        // Act
        var errors = notifier.sendNotification(recipients);

        // Assert
        assertThat(errors).isEmpty();
        verify(notificationService, times(1)).sendMail(
            eq(email1.getTargetEmail()), eq(email1.getEmailTemplate()), eq(email1.getParameters()), eq(email1.getReference())
        );
        verify(notificationService, times(1)).sendMail(
            eq(email2.getTargetEmail()), eq(email2.getEmailTemplate()), eq(email2.getParameters()), eq(email2.getReference())
        );
    }

    @Test
    void shouldHandleNotificationExceptions() {
        // Arrange
        EmailDTO email1 = EmailDTO.builder()
            .targetEmail("recipient1@example.com")
            .emailTemplate("template1")
            .parameters(Map.of("key1", "value1"))
            .reference("ref1")
            .build();

        EmailDTO email2 = EmailDTO.builder()
            .targetEmail("recipient2@example.com")
            .emailTemplate("template2")
            .parameters(Map.of("key2", "value2"))
            .reference("ref2")
            .build();

        Set<EmailDTO> recipients = Set.of(email1, email2);

        doThrow(new NotificationException(new Exception("Failed to send email to recipient2@example.com")))
            .when(notificationService).sendMail(
                eq(email2.getTargetEmail()), eq(email2.getEmailTemplate()), eq(email2.getParameters()), eq(email2.getReference())
            );

        // Act
        var errors = notifier.sendNotification(recipients);

        // Assert
        assertThat(errors).containsExactly("java.lang.Exception: Failed to send email to recipient2@example.com");
        verify(notificationService, times(1)).sendMail(
            eq(email1.getTargetEmail()), eq(email1.getEmailTemplate()), eq(email1.getParameters()), eq(email1.getReference())
        );
        verify(notificationService, times(1)).sendMail(
            eq(email2.getTargetEmail()), eq(email2.getEmailTemplate()), eq(email2.getParameters()), eq(email2.getReference())
        );
    }
}
