package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.model.TaskList;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationTemplateRepository;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationTemplateService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DashboardControllerTest extends BaseIntegrationTest {

    @Autowired
    private DashboardNotificationService dashboardNotificationService;

    @Autowired
    private DashboardNotificationTemplateService dashboardNotificationTemplateService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    private final UUID id = UUID.randomUUID();

    private final String getTaskListUrl = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    private final String[] notificationsToBeDeleted = {"notification"};

    private final NotificationTemplateEntity template =
        new NotificationTemplateEntity(1L, "Defendant", "name", notificationsToBeDeleted,
                                       "English title", "Welsh title", "English body",
                                       "Welsh body", new Date(), "");
    private final NotificationEntity notification =
        new NotificationEntity(id, template, "1234", "name", "Claimant",
                               "English Title", "Welsh Title", "English body",
                               "Welsh body", "Params", "createdBy", new Date(),
                               "updatedBy", new Date());

    private final String endPointUrlGet = "/dashboard/notifications/{uuid}";

    private final String endPointUrlDelete = "/dashboard/notifications/{unique-notification-identifier}";

    @Nested
    class GenericTests {
        @Test
        @SneakyThrows
        void shouldReturnErrorWhenNotUuidFormat() {

            doGet(BEARER_TOKEN, endPointUrlGet,  "1234")
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetTests {
        @Test
        @SneakyThrows
        @Sql("/scripts/dashboardNotifications/get_task_list_data.sql")
        void shouldReturnTaskListForGiveCaseReferenceAndRole() {

            doGet(BEARER_TOKEN, getTaskListUrl,  "123","defendant")
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(getTaskLists())));
        }
    }

    @Nested
    class DeleteTests {

        @BeforeEach
        void setUp() {
            notificationTemplateRepository.save(template);
            notificationRepository.save(notification);
        }

        @Test
        @SneakyThrows
        void shouldReturnOkWhenDeletingExistingEntity() {

            assertTrue(notificationRepository.findById(id).isPresent());

            doDelete(BEARER_TOKEN, null, endPointUrlDelete, id.toString())
                .andExpect(status().isOk());

            assertFalse(notificationRepository.findById(id).isPresent());
        }

        @Test
        @SneakyThrows
        void shouldReturnNotFoundWhenNoMatchingId() {
            assertTrue(notificationRepository.findById(id).isPresent());

            doDelete(BEARER_TOKEN, null, endPointUrlDelete, "")
                .andExpect(status().isNotFound());

            assertTrue(notificationRepository.findById(id).isPresent());
        }
    }

    private List<TaskList> getTaskLists() {

        List<TaskList> taskList = new ArrayList<>();
        taskList.add(TaskList.builder().id(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e5")).taskNameCy("task_name_cy").taskNameEn("task_name_en").taskOrder(0).categoryCy("category_cy").categoryEn("category_en")
                         .role("defendant").currentStatus(0).nextStatus(1).hintTextCy("hint_text_cy").hintTextEn("hint_text_en").reference("123")
                         .updatedBy("Test").build());

        return taskList;
    }
}
