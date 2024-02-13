package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.model.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/scripts/dashboardNotifications/get_notifications_data.sql")
public class DashboardNotificationControllerTest extends BaseIntegrationTest {

    @Test
    @SneakyThrows
    void shouldReturnNotificationListWhenRequestingWithReferenceAndRole() {
        String getNotificationsEndpoint = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";
        doGet(BEARER_TOKEN, getNotificationsEndpoint, "123", "defendant")
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(getNotificationList())));
    }

    private List<Notification> getNotificationList() {
        List<Notification> notificationList = new ArrayList<>();

        notificationList.add(Notification.builder().id(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e5"))
                                 .titleEn("title_en")
                                 .titleCy("title_cy")
                                 .descriptionEn("description_en")
                                 .descriptionCy("description_cy").build());

        return notificationList;
    }
}
