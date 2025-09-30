package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes =
    NotificationService.class
)
class NotificationServiceTest {

    @MockBean
    private  NotificationClient notificationClient;

    @Autowired
    private NotificationService notificationService;

    @Nested class Notification {

        private static final String DUMMY_EMAIL = "example@co.com";
        private static final String TEMPLATE_ID = "fake-id";
        private static final String REFERENCE = "reference";
        private static final String NULL_EMAIL = "";
        private static final String NULL_TEMPLATE_ID = "";

        @Test
        public void shouldThrowExceptionWhenEmailIsNotPresentWhenEmailIsSent() throws Exception {
            Map<String, String> personalization = Map.of("key", "val");
            Mockito.doThrow(NotificationException.class)
                .when(notificationClient)
                .sendEmail(
                    TEMPLATE_ID,
                    NULL_EMAIL,
                    personalization,
                    REFERENCE
                );

            assertThrows(NotificationException.class, () -> notificationService
                             .sendMail(
                                 NULL_EMAIL,
                                 TEMPLATE_ID,
                                 personalization,
                                 REFERENCE
                             ));

        }

        @Test
        public void shouldThrowExceptionWhenTemplateIsNotPresentWhenEmailIsSent() throws Exception {
            Map<String, String> personalization = Map.of("key", "val");

            Mockito.doThrow(NotificationException.class)
                .when(notificationClient)
                .sendEmail(
                    NULL_TEMPLATE_ID,
                    DUMMY_EMAIL,
                    personalization,
                    REFERENCE
                );

            assertThrows(NotificationException.class, () -> notificationService
                             .sendMail(
                                 DUMMY_EMAIL,
                                 NULL_TEMPLATE_ID,
                                 personalization,
                                 REFERENCE
                             ));

        }

        @Test
        public void shouldNotThrowExceptionWhenValuesArePresentWhenEmailIsSent() throws NotificationClientException {
            Map<String, String> personalization = Map.of("key", "val");

            Mockito.doThrow(NotificationException.class)
                .when(notificationClient)
                .sendEmail(
                    NULL_TEMPLATE_ID,
                    DUMMY_EMAIL,
                    personalization,
                    REFERENCE
                );

            assertDoesNotThrow(() -> notificationService.sendMail(
                DUMMY_EMAIL,
                TEMPLATE_ID,
                personalization,
                REFERENCE
            ));
        }
    }
}
