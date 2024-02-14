package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql("/scripts/dashboardNotifications/notifications_data.sql")
public class RecordNotificationClickControllerTest extends BaseIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private final UUID id = UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e5");

    private final String endPointUrlDelete = "/dashboard/notifications/{unique-notification-identifier}";

    @Test
    @SneakyThrows
    void shouldReturnOkWhenDeletingExistingEntity() {

        assertTrue(notificationRepository.findById(id).isPresent());

        doDelete(BEARER_TOKEN, null, endPointUrlDelete, id.toString())
            .andExpect(status().isOk());

        assertFalse(notificationRepository.findById(id).isPresent());
    }
}
