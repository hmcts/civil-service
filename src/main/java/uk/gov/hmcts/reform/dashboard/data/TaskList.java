package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskList {

    private UUID id;

    private String reference;

    private String currentStatusEn;

    private String currentStatusCy;

    private String nextStatusEn;

    private String nextStatusCy;

    private String taskNameEn;

    private String hintTextEn;

    private String taskNameCy;

    private String hintTextCy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private String updatedBy;

    private Map<String, Object> messageParams;

    private String categoryEn;

    private String categoryCy;

    private String role;

    private int taskOrder;

    public static TaskList from(TaskListEntity taskListEntity) {
        return new TaskList(
            taskListEntity.getId(),
            taskListEntity.getReference(),
            TaskStatus.getTaskStatusByPlaceValue(taskListEntity.getCurrentStatus()).getName(),
            TaskStatus.getTaskStatusByPlaceValue(taskListEntity.getCurrentStatus()).getWelshName(),
            TaskStatus.getTaskStatusByPlaceValue(taskListEntity.getNextStatus()).getName(),
            TaskStatus.getTaskStatusByPlaceValue(taskListEntity.getNextStatus()).getWelshName(),
            taskListEntity.getTaskNameEn(),
            taskListEntity.getHintTextEn(),
            taskListEntity.getTaskNameCy(),
            taskListEntity.getHintTextCy(),
            taskListEntity.getCreatedAt(),
            taskListEntity.getUpdatedAt(),
            taskListEntity.getUpdatedBy(),
            taskListEntity.getMessageParams(),
            taskListEntity.getTaskItemTemplate().getCategoryEn(),
            taskListEntity.getTaskItemTemplate().getCategoryCy(),
            taskListEntity.getTaskItemTemplate().getRole(),
            taskListEntity.getTaskItemTemplate().getTaskOrder()
        );
    }

}
