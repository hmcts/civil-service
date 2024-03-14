package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql("/scripts/dashboardNotifications/get_notifications_data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GetNotificationControllerTest extends BaseIntegrationTest {

    @Test
    @SneakyThrows
    void shouldReturnNotificationListWhenRequestingWithReferenceAndRole() {
        String getNotificationsEndpoint = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";
        doGet(BEARER_TOKEN, getNotificationsEndpoint, "127", "defendant")
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(getNotificationList())));
    }

    private List<Notification> getNotificationList() {
        List<Notification> notificationList = new ArrayList<>();

        notificationList.add(Notification.builder().id(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e5"))
                                 .titleEn("title_en")
                                 .titleCy("title_cy")
                                 .descriptionEn("description_en")
                                 .descriptionCy("description_cy")
                                 .timeToLive("Click").build());

        return notificationList;
    }
}
