package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GetTaskListControllerTest extends BaseIntegrationTest {

    private static final String GET_TASK_LIST_URL = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    @Test
    @SneakyThrows
    @Sql("/scripts/dashboardNotifications/get_task_list.sql")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnTaskListForGiveCaseReferenceAndRole() {

        doGet(BEARER_TOKEN, GET_TASK_LIST_URL, "124", "defendant")
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(getTaskLists())));
    }

    private List<TaskList> getTaskLists() {

        LocalDateTime date = LocalDateTime.parse("2024-01-01T10:15:30");
        ZonedDateTime zdt = date.atZone(ZoneId.of("Etc/UTC"));
        OffsetDateTime odt = zdt.toOffsetDateTime();

        TaskList taskListItem = new TaskList();
        taskListItem.setId(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e8"));
        taskListItem.setTaskNameCy("task_name_cy");
        taskListItem.setTaskNameEn("task_name_en");
        taskListItem.setTaskOrder(0);
        taskListItem.setCategoryCy("category_cy");
        taskListItem.setCategoryEn("category_en");
        taskListItem.setRole("defendant");
        taskListItem.setCurrentStatusEn(TaskStatus.NOT_AVAILABLE_YET.getName());
        taskListItem.setCurrentStatusCy(TaskStatus.NOT_AVAILABLE_YET.getWelshName());
        taskListItem.setNextStatusEn(TaskStatus.IN_PROGRESS.getName());
        taskListItem.setNextStatusCy(TaskStatus.IN_PROGRESS.getWelshName());
        taskListItem.setHintTextCy("hint_text_cy");
        taskListItem.setHintTextEn("hint_text_en");
        taskListItem.setReference("124");
        taskListItem.setUpdatedBy("Test");
        taskListItem.setCreatedAt(odt);
        return List.of(taskListItem);
    }
}
