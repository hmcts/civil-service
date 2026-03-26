package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.UpdateDashboardTaskCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.UUID;

@Component
@Slf4j
public class UpdateDashboardTaskStatus extends MigrationTask<UpdateDashboardTaskCaseReference> {

    private static final String DEFAULT_UPDATED_BY = "UpdateDashboardTaskStatus";

    public final TaskListService taskListService;

    public UpdateDashboardTaskStatus(TaskListService taskListEntity1) {
        super(UpdateDashboardTaskCaseReference.class);
        this.taskListService = taskListEntity1;
    }

    @Override
    protected String getEventSummary() {
        return "Update dashboard task status";
    }

    protected CaseData migrateCaseData(CaseData caseData, UpdateDashboardTaskCaseReference caseRef) {
        if (caseData == null || caseRef.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        }
        TaskListEntity taskItemEntity = new TaskListEntity();
        taskItemEntity.setId(UUID.fromString(caseRef.getTaskListId()));
        taskItemEntity.setTaskNameEn(caseRef.getTaskNameEn());
        taskItemEntity.setTaskNameCy(caseRef.getTaskNameCy());
        taskItemEntity.setCurrentStatus(Integer.parseInt(caseRef.getCurrentStatus()));
        taskItemEntity.setNextStatus(Integer.parseInt(caseRef.getNextStatus()));
        taskItemEntity.setUpdatedBy(caseRef.getUpdatedBy() != null ? caseRef.getUpdatedBy() : DEFAULT_UPDATED_BY);

        log.info(
            "Updating dashboard task id={} for caseReference={} taskNameEn={} taskNameCy={} currentStatus={} nextStatus={} updatedBy={}",
            taskItemEntity.getId(),
            caseRef.getCaseReference(),
            taskItemEntity.getTaskNameEn(),
            taskItemEntity.getTaskNameCy(),
            taskItemEntity.getCurrentStatus(),
            taskItemEntity.getNextStatus(),
            taskItemEntity.getUpdatedBy()
        );

        taskListService.updateTask(taskItemEntity);

        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task is used to update a dashboard task status";
    }

    @Override
    protected String getTaskName() {
        return "UpdateDashboardTaskStatus";
    }
}
