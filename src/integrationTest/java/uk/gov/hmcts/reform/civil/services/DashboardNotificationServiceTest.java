package uk.gov.hmcts.reform.civil.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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

class DashboardNotificationServiceTest extends BaseIntegrationTest {

    @MockBean
    private DashboardNotificationService dashboardNotificationService;

    @MockBean
    private DashboardNotificationTemplateService dashboardNotificationTemplateService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    private final UUID id = UUID.randomUUID();

    private final String[] notificationsToBeDeleted = {"notification"};

    private final NotificationTemplateEntity template = new NotificationTemplateEntity(1L, "Defendant", "name", notificationsToBeDeleted, "English", "Welsh", new Date(), "");
    private final NotificationEntity notification = new NotificationEntity(id, template, "1234", "name", "Claimant", "English", "Welsh", "Params", "createdBy", new Date(), "updatedBy", new Date());

    @BeforeEach
    void setUp() {
        dashboardNotificationService = new DashboardNotificationService(notificationRepository);
        dashboardNotificationTemplateService = new DashboardNotificationTemplateService(notificationTemplateRepository);
    }

    @Nested
    class DeleteTests {

        @Test
        void deleteNotificationById() {

            notificationTemplateRepository.save(template);
            notificationRepository.save(notification);

            boolean notificationPresentBefore = notificationRepository.findById(id).isPresent();

            dashboardNotificationService.delete(id);

            boolean notificationPresentAfter = notificationRepository.findById(id).isPresent();

            assertTrue(notificationPresentBefore);
            assertFalse(notificationPresentAfter);
        }
    }
}
