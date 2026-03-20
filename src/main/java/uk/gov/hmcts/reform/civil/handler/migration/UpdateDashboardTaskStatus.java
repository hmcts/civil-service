package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.UpdateDashboardTaskCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.UUID;

@Component
public class UpdateDashboardTaskStatus extends MigrationTask<UpdateDashboardTaskCaseReference> {

    public final TaskListService taskListService;

    public UpdateDashboardTaskStatus(TaskListService taskListEntity1) {
        super(UpdateDashboardTaskCaseReference.class);
        this.taskListService = taskListEntity1;
    }

    @Override
    protected String getEventSummary() {
        return "Assign a case role to a case";
    }

    protected CaseData migrateCaseData(CaseData caseData, UpdateDashboardTaskCaseReference caseRef) {
        if (caseData == null || caseRef.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        }
        TaskListEntity taskItemEntity = new TaskListEntity();
        taskItemEntity.setId(UUID.fromString(caseRef.getTaskItemTemplateId()));
        taskItemEntity.setCurrentStatus(Integer.parseInt(caseRef.getCurrentStatus()));
        taskItemEntity.setNextStatus(Integer.parseInt(caseRef.getNextStatus()));

        taskListService.updateTask(taskItemEntity);

        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task is used to assign a case role to a case";
    }

    @Override
    protected String getTaskName() {
        return "AssignUserWithACaseRole";
    }
}
