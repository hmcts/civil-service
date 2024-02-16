package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.TaskList;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GetTaskListControllerTest extends BaseIntegrationTest {

    private final String getTaskListUrl = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    private final String endPointUrlGet = "/dashboard/notifications/{uuid}";

    @Test
    @SneakyThrows
    void shouldReturnErrorWhenNotUuidFormat() {

        doGet(BEARER_TOKEN, endPointUrlGet, "1234")
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @Sql("/scripts/dashboardNotifications/get_task_list.sql")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnTaskListForGiveCaseReferenceAndRole() {

        doGet(BEARER_TOKEN, getTaskListUrl, "124", "defendant")
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(getTaskLists())));
    }

    private List<TaskList> getTaskLists() {

        LocalDateTime date = LocalDateTime.parse("2024-01-01T10:15:30");
        ZonedDateTime zdt = date.atZone(ZoneId.of("Etc/UTC"));
        OffsetDateTime odt = zdt.toOffsetDateTime();

        List<TaskList> taskList = new ArrayList<>();
        taskList.add(TaskList.builder().id(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e8")).taskNameCy(
                "task_name_cy").taskNameEn("task_name_en").taskOrder(0).categoryCy("category_cy").categoryEn("category_en")
                         .role("defendant").currentStatus(0).nextStatus(1).hintTextCy("hint_text_cy").hintTextEn(
                "hint_text_en").reference("124")
                         .updatedBy("Test").createdAt(odt).build()
        );

        return taskList;
    }
}
