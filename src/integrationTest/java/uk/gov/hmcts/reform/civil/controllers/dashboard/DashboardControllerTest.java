package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.model.Notification;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/scripts/dashboardNotifications/get_notifications_data.sql")
public class DashboardControllerTest extends BaseIntegrationTest {

    @MockBean
    private NotificationRepository notificationRepository;

    private final String endPointUrl = "/dashboard/notifications/{uuid}";

    @Test
    @SneakyThrows
    void shouldReturnOkWhenGettingExistingEntity() {
        UUID id = UUID.randomUUID();
        NotificationEntity notification = new NotificationEntity(id, new NotificationTemplateEntity(), "12345", "name", "Defendant", "en", "cy", "en", "cy", "params", "createdBy",
                                                                 new Date(),  "updatedBy", new Date());

        when(notificationRepository.findById(any())).thenReturn(Optional.of(notification));

        doGet(BEARER_TOKEN, endPointUrl, UUID.randomUUID().toString())
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(notification)));
    }

    @Test
    @SneakyThrows
    void shouldReturnPreconditionFailedErrorWhenIllegalArgumentExceptionIsThrown() {
        doThrow(new IllegalArgumentException()).when(notificationRepository).findById(any());

        //I don't think it should throw this specific error, but I'll leave it to the person working on get notifications to fix.
        doGet(BEARER_TOKEN, endPointUrl, UUID.randomUUID().toString())
            .andExpect(status().isPreconditionFailed());
    }

    @Test
    @SneakyThrows
    void shouldReturnErrorWhenNotUuidFormat() {

        doGet(BEARER_TOKEN, endPointUrl,  "1234")
            .andExpect(status().isBadRequest());
    }

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
