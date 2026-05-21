package uk.gov.hmcts.reform.civil.workflow.dashboard;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationActionRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

@SuppressWarnings("java:S6813")
public abstract class DashboardWorkflowIntegrationTest extends WorkflowIntegrationTest {

    @Autowired
    private DashboardNotificationsRepository dashboardNotificationsRepository;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private NotificationActionRepository notificationActionRepository;

    @AfterEach
    void cleanupDashboardState() {
        taskListRepository.deleteAll();
        notificationActionRepository.deleteAll();
        dashboardNotificationsRepository.deleteAll();
    }
}
