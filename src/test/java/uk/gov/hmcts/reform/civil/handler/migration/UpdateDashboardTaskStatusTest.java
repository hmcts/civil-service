package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.UpdateDashboardTaskCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

class UpdateDashboardTaskStatusTest {

    @Mock
    private TaskListService taskListService;

    private UpdateDashboardTaskStatus updateDashboardTaskStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        updateDashboardTaskStatus = new UpdateDashboardTaskStatus(taskListService);
    }

    @Test
    void shouldUpdateDashboardTaskSuccessfully() {
        String taskId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
        UpdateDashboardTaskCaseReference reference = new UpdateDashboardTaskCaseReference();
        reference.setCaseReference("12345");
        reference.setTaskListId(taskId);
        reference.setTaskNameEn("English task name");
        reference.setTaskNameCy("Welsh task name");
        reference.setCurrentStatus("1");
        reference.setNextStatus("2");
        reference.setUpdatedBy("migration-user");

        CaseData caseData = new CaseData().build();

        CaseData result = updateDashboardTaskStatus.migrateCaseData(caseData, reference);

        assertSame(caseData, result);
        verify(taskListService).updateTask(argThat((TaskListEntity task) ->
            UUID.fromString(taskId).equals(task.getId())
                && "English task name".equals(task.getTaskNameEn())
                && "Welsh task name".equals(task.getTaskNameCy())
                && task.getCurrentStatus() == 1
                && task.getNextStatus() == 2
                && "migration-user".equals(task.getUpdatedBy())
        ));
    }

    @Test
    void shouldDefaultUpdatedByWhenNotProvided() {
        String taskId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
        UpdateDashboardTaskCaseReference reference = new UpdateDashboardTaskCaseReference();
        reference.setCaseReference("12345");
        reference.setTaskListId(taskId);
        reference.setCurrentStatus("1");
        reference.setNextStatus("2");

        updateDashboardTaskStatus.migrateCaseData(new CaseData().build(), reference);

        verify(taskListService).updateTask(argThat((TaskListEntity task) ->
            UUID.fromString(taskId).equals(task.getId())
                && "UpdateDashboardTaskStatus".equals(task.getUpdatedBy())
        ));
    }

    @Test
    void shouldExposeUniqueTaskName() {
        assertEquals("UpdateDashboardTaskStatus", updateDashboardTaskStatus.getTaskName());
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        UpdateDashboardTaskCaseReference reference = new UpdateDashboardTaskCaseReference();
        reference.setCaseReference("12345");

        assertThrows(IllegalArgumentException.class, () ->
            updateDashboardTaskStatus.migrateCaseData(null, reference)
        );
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        UpdateDashboardTaskCaseReference reference = new UpdateDashboardTaskCaseReference();
        reference.setCaseReference(null);

        assertThrows(IllegalArgumentException.class, () ->
            updateDashboardTaskStatus.migrateCaseData(new CaseData().build(), reference)
        );
    }
}
