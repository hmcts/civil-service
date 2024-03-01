package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
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
        return TaskList.builder()
            .id(taskListEntity.getId())
            .reference(taskListEntity.getReference())
            .currentStatusEn(TaskStatus.getTaskStatusByPlaceValue(taskListEntity.getCurrentStatus()).getName())
            .currentStatusCy(TaskStatus.getTaskStatusByPlaceValue(taskListEntity.getCurrentStatus()).getWelshName())
            .nextStatusEn(TaskStatus.getTaskStatusByPlaceValue(taskListEntity.getNextStatus()).getName())
            .nextStatusCy(TaskStatus.getTaskStatusByPlaceValue(taskListEntity.getNextStatus()).getWelshName())
            .taskNameEn(taskListEntity.getTaskNameEn())
            .hintTextEn(taskListEntity.getHintTextEn())
            .taskNameCy(taskListEntity.getTaskNameCy())
            .hintTextCy(taskListEntity.getHintTextCy())
            .createdAt(taskListEntity.getCreatedAt())
            .updatedBy(taskListEntity.getUpdatedBy())
            .updatedAt(taskListEntity.getUpdatedAt())
            .messageParams(taskListEntity.getMessageParams())
            .categoryEn(taskListEntity.getTaskItemTemplate().getCategoryEn())
            .categoryCy(taskListEntity.getTaskItemTemplate().getCategoryCy())
            .role(taskListEntity.getTaskItemTemplate().getRole())
            .taskOrder(taskListEntity.getTaskItemTemplate().getTaskOrder())
            .build();
    }

}
