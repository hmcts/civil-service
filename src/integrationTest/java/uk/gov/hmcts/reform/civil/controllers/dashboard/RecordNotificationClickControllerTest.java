package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class RecordNotificationClickControllerTest extends BaseIntegrationTest {

    public static final String CCD_CASE_ID = "130";
    public static final String ACTION_PAERFORMED = "Click";
    private static final UUID NOTIFICATION_ID = UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b945");
    private static final String NOTIFICATION_CLICK_END_POINT
        = "/dashboard/notifications/{unique-notification-identifier}";

    @Autowired
    private DashboardNotificationsRepository dashboardNotificationsRepository;

    @Test
    @SneakyThrows
    @Sql("/scripts/dashboardNotifications/record_notification_click.sql")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRecordNotificationClick() {
        doPut(BEARER_TOKEN, null, NOTIFICATION_CLICK_END_POINT, NOTIFICATION_ID.toString())
            .andExpect(status().isOk());

        Optional<DashboardNotificationsEntity> notification = dashboardNotificationsRepository.findById(NOTIFICATION_ID);
        assertThat(notification).isPresent();
        DashboardNotificationsEntity dashboardNotificationsEntity = notification.get();
        NotificationActionEntity notificationAction = dashboardNotificationsEntity.getNotificationAction();
        assertThat(notificationAction.getDashboardNotification().getId())
            .isEqualTo(dashboardNotificationsEntity.getId());
        assertThat(notificationAction.getActionPerformed()).isEqualTo(ACTION_PAERFORMED);
        assertThat(notificationAction.getReference()).isEqualTo(CCD_CASE_ID);
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void shouldReturnUnauthorisedWhenBearerTokenMissing() {

        doDelete("", null, NOTIFICATION_CLICK_END_POINT, NOTIFICATION_ID.toString())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void shouldReturnBadRequestWhenUuidNotInCorrectFormat() {

        doDelete(BEARER_TOKEN, null, NOTIFICATION_CLICK_END_POINT, "126")
            .andExpect(status().isBadRequest());

    }
}
