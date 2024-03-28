package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql("/scripts/dashboardNotifications/record_task_item_click.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RecordTaskItemClickControllerTest extends BaseIntegrationTest {

    private static final UUID TASK_ITEM_IDENTIFIER = UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e7");
    private static final String END_POINT_URL = "/dashboard/taskList/{task-item-identifier}";

    @Autowired
    private TaskListRepository taskListRepository;

    @Test
    @SneakyThrows
    void shouldReturnOkWithTaskItemStatusChangedWhenTaskItemClickRecorded() {

        TaskListEntity taskListEntity = taskListRepository.findById(TASK_ITEM_IDENTIFIER).get();

        assertEquals(taskListEntity.getCurrentStatus(), 1);
        taskListEntity.setCurrentStatus(6);

        doPut(BEARER_TOKEN, null, END_POINT_URL, TASK_ITEM_IDENTIFIER)
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(taskListEntity)));
    }

}
