package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql("/scripts/dashboardNotifications/task_list_data.sql")
public class RecordTaskItemClickControllerTest extends BaseIntegrationTest {

    @Autowired
    private TaskListRepository taskListRepository;

    private final String ccdCaseIdentifier = "123";

    private final String name = "name";

    private final String roleType = "defendant";

    private final String endPointUrlPut = "/dashboard/taskList/{ccd-case-identifier}/{templateName}/role/{role-type}";

    @Test
    @SneakyThrows
    void shouldReturnOkWithTaskItemStatusChangedWhenTaskItemClickRecorded() {

        TaskListEntity taskListEntity = taskListRepository
            .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateName(ccdCaseIdentifier, roleType, name).get();

        assertEquals(taskListEntity.getCurrentStatus(), 0);
        taskListEntity.setCurrentStatus(1);

        doPut(BEARER_TOKEN, null, endPointUrlPut, ccdCaseIdentifier, name, roleType)
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(taskListEntity)));

    }

}
