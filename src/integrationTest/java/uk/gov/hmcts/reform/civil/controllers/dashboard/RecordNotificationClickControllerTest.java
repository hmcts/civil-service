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
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class RecordNotificationClickControllerTest extends BaseIntegrationTest {

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
        DashboardNotificationsEntity notificationEntity = notification.get();
        assertThat(notificationEntity.getClickedBy()).isEqualTo("Claimant test");
        assertThat(notificationEntity.getClickedAt()).isCloseTo(
            OffsetDateTime.now(ZoneOffset.UTC), within(5, ChronoUnit.MINUTES)
        );
    }

    @Test
    @SneakyThrows
    void shouldReturnUnauthorisedWhenBearerTokenMissing() {
        when(userRequestAuthorizerMock.authorise(any())).thenReturn(null);
        doPut("", null, NOTIFICATION_CLICK_END_POINT, NOTIFICATION_ID.toString())
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWhenUuidNotInCorrectFormat() {
        doPut(BEARER_TOKEN, null, NOTIFICATION_CLICK_END_POINT, "126")
            .andExpect(status().isBadRequest());
    }
}
