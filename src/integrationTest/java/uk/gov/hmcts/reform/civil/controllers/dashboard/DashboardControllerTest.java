package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationTemplateRepository;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationTemplateService;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardControllerTest extends BaseIntegrationTest {

    @Autowired
    private DashboardNotificationService dashboardNotificationService;

    @Autowired
    private DashboardNotificationTemplateService dashboardNotificationTemplateService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    private final UUID id = UUID.randomUUID();

    private final String[] notificationsToBeDeleted = {"notification"};

    private final NotificationTemplateEntity template =
        new NotificationTemplateEntity(1L, "Defendant", "name", notificationsToBeDeleted,
                                       "English title", "Welsh title", "English body",
                                       "Welsh body", new Date(), "");
    private final NotificationEntity notification =
        new NotificationEntity(id, template, "1234", "name", "Claimant",
                               "English Title", "Welsh Title", "English body",
                               "Welsh body", "Params", "createdBy", new Date(),
                               "updatedBy", new Date());

    @BeforeEach
    void setUp() {
        notificationTemplateRepository.save(template);
        notificationRepository.save(notification);
    }

    private final String endPointUrlGet = "/dashboard/notifications/{uuid}";

    private final String endPointUrlDelete = "/dashboard/notifications/{unique-notification-identifier}";

    @Nested
    class GenericTests {
        @Test
        @SneakyThrows
        void shouldReturnErrorWhenNotUuidFormat() {

            doGet(BEARER_TOKEN, endPointUrlGet,  "1234")
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetTests {

    }

    @Nested
    class DeleteTests {
        @Test
        @SneakyThrows
        void shouldReturnOkWhenDeletingExistingEntity() {

            assertTrue(notificationRepository.findById(id).isPresent());

            doDelete(BEARER_TOKEN, null, endPointUrlDelete, id.toString())
                .andExpect(status().isOk());

            assertFalse(notificationRepository.findById(id).isPresent());
        }

        @Test
        @SneakyThrows
        void shouldReturnNotFoundWhenNoMatchingId() {
            assertTrue(notificationRepository.findById(id).isPresent());

            doDelete(BEARER_TOKEN, null, endPointUrlDelete, "")
                .andExpect(status().isNotFound());

            assertTrue(notificationRepository.findById(id).isPresent());
        }
    }
}
