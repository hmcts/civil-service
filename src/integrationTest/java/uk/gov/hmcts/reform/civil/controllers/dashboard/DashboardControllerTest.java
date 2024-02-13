package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.model.TaskList;
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


@Sql("/scripts/dashboardNotifications/get_task_list_data.sql")
public class DashboardControllerTest extends BaseIntegrationTest {

    @MockBean
    private NotificationRepository notificationRepository;

    private final String endPointUrl = "/dashboard/notifications/{uuid}";

    private final String getTaskListUrl = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    @Test
    @SneakyThrows
    void shouldReturnOkWhenGettingExistingEntity() {
        UUID id = UUID.randomUUID();
        NotificationEntity notification = new NotificationEntity(id, new NotificationTemplateEntity(), "12345", "name", "Defendant", "en", "cy", "params", "createdBy",
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
    void shouldReturnTaskListForGiveCaseReferenceAndRole() {

        doGet(BEARER_TOKEN, getTaskListUrl,  "123","defendant")
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(getTaskLists())));
    }

    private List<TaskList> getTaskLists() {

        List<TaskList> taskList = new ArrayList<>();
        taskList.add(TaskList.builder().id(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e5")).taskNameCy("task_name_cy").taskNameEn("task_name_en").taskOrder(0).categoryCy("category_cy").categoryEn("category_en")
                         .role("defendant").currentStatus(0).nextStatus(1).hintTextCy("hint_text_cy").hintTextEn("hint_text_en").reference("123")
                         .updatedBy("Test").build());

        return taskList;
    }
}
