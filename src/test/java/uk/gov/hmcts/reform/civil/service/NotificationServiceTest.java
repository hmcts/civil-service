package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationService notificationService;

    final String template = "template";
    final String reference = "reference";
    Map<String, String> params = Map.of("mock", "params");
    List<String> recipients;

    @BeforeEach
    void setUp() throws NotificationClientException {
        recipients = Arrays.asList("email-1@example.com");
    }

    @Nested
    class SendNotifications {

        @Test
        void shouldSendNotifications_whenInvoked() throws NotificationClientException {
            when(notificationClient.sendEmail(template, recipients.get(0), params, reference)).thenReturn(null);

            notificationService.sendNotifications(recipients, template, params, reference);

            verify(notificationClient).sendEmail(template, recipients.get(0), params, reference);
        }

        @Test
        void shouldSendNotifications_whenInvokedWithMultipleRecipients() throws NotificationClientException {
            recipients = Arrays.asList("email-1@example.com", "email-2@example.com");
            when(notificationClient.sendEmail(template, recipients.get(0), params, reference)).thenReturn(null);
            when(notificationClient.sendEmail(template, recipients.get(1), params, reference)).thenReturn(null);

            notificationService.sendNotifications(recipients, template, params, reference);

            verify(notificationClient).sendEmail(template, recipients.get(0), params, reference);
            verify(notificationClient).sendEmail(template, recipients.get(1), params, reference);
        }

        @Test
        void shouldThrowNotificationException_ifNotificationClientErrors_whenInvoked()
            throws NotificationClientException {
            when(notificationClient.sendEmail(template, recipients.get(0), params, reference))
                .thenThrow(NotificationClientException.class);

            Assertions.assertThrows(
                NotificationException.class,
                () -> notificationService.sendNotifications(recipients, template, params, reference));
        }
    }

    @Nested
    class SendEmail {

        @Test
        void shouldSendNotifications_whenInvoked() throws NotificationClientException {
            when(notificationClient.sendEmail(template, recipients.get(0), params, reference)).thenReturn(null);

            notificationService.sendMail(recipients.get(0), template, params, reference);

            verify(notificationClient).sendEmail(template, recipients.get(0), params, reference);
        }

        @Test
        void shouldThrowNotificationException_ifNotificationClientErrors_whenInvoked()
            throws NotificationClientException {
            when(notificationClient.sendEmail(template, recipients.get(0), params,
                                              reference
            )).thenThrow(NotificationClientException.class);
            Assertions.assertThrows(
                NotificationException.class,
                () -> notificationService.sendNotifications(recipients, template, params, reference));
        }

    }
}
