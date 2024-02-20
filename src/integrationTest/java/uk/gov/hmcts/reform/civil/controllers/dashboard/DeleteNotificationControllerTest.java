package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DeleteNotificationControllerTest extends BaseIntegrationTest {

    @Autowired
    private DashboardNotificationsRepository dashboardNotificationsRepository;

    private final UUID id = UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e6");

    private final String endPointUrlDelete = "/dashboard/notifications/{unique-notification-identifier}";

    @Test
    @SneakyThrows
    @Sql("/scripts/dashboardNotifications/delete_notification.sql")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnOkWhenDeletingExistingEntity() {

        assertTrue(dashboardNotificationsRepository.findById(id).isPresent());

        doDelete(BEARER_TOKEN, null, endPointUrlDelete, id.toString())
            .andExpect(status().isOk());

        assertFalse(dashboardNotificationsRepository.findById(id).isPresent());
    }

    @Test
    @SneakyThrows
    void shouldReturnUnauthorisedWhenBearerTokenMissing() {

        doDelete("", null, endPointUrlDelete, id.toString())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWhenUuidNotInCorrectFormat() {

        doDelete(BEARER_TOKEN, null, endPointUrlDelete, "126")
            .andExpect(status().isBadRequest());

    }
}
