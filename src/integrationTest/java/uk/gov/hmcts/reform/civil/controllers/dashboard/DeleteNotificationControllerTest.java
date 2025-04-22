package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenMissingException;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DeleteNotificationControllerTest extends BaseIntegrationTest {

    private static final UUID NOTIFICATION_IDENTIFIER = UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e6");
    private static final String END_POINT_URL = "/dashboard/notifications/{unique-notification-identifier}";

    @Autowired
    private DashboardNotificationsRepository dashboardNotificationsRepository;

    @Test
    @SneakyThrows
    @Sql("/scripts/dashboardNotifications/delete_notification.sql")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnOkWhenDeletingExistingEntity() {

        assertTrue(dashboardNotificationsRepository.findById(NOTIFICATION_IDENTIFIER).isPresent());

        doDelete(BEARER_TOKEN, null, END_POINT_URL, NOTIFICATION_IDENTIFIER.toString())
            .andExpect(status().isOk());

        assertFalse(dashboardNotificationsRepository.findById(NOTIFICATION_IDENTIFIER).isPresent());
    }

    @Test
    @SneakyThrows
    void shouldReturnUnauthorisedWhenBearerTokenMissing() {

        when(userRequestAuthorizerMock.authorise(any())).thenThrow(BearerTokenMissingException.class);

        doDelete("", null, END_POINT_URL, NOTIFICATION_IDENTIFIER.toString())
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWhenUuidNotInCorrectFormat() {

        doDelete(BEARER_TOKEN, null, END_POINT_URL, "126")
            .andExpect(status().isBadRequest());

    }
}
