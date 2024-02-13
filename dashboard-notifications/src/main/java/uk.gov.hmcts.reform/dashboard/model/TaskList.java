package uk.gov.hmcts.reform.dashboard.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskList {

    private UUID id;

    private String reference;

    private int currentStatus;

    private int nextStatus;

    private String taskNameEn;

    private String hintTextEn;

    private String taskNameCy;

    private String hintTextCy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private String updatedBy;

    private JsonNode messageParm;

    private String categoryEn;

    private String categoryCy;

    private String role;

    private int taskOrder;

    public static TaskList from(TaskListEntity taskListEntity) {
        return TaskList.builder()
            .id(taskListEntity.getId())
            .reference(taskListEntity.getReference())
            .currentStatus(taskListEntity.getCurrentStatus())
            .nextStatus(taskListEntity.getNextStatus())
            .taskNameEn(taskListEntity.getTaskNameEn())
            .hintTextEn(taskListEntity.getHintTextEn())
            .taskNameCy(taskListEntity.getTaskNameCy())
            .hintTextCy(taskListEntity.getHintTextCy())
            .createdAt(taskListEntity.getCreatedAt())
            .updatedBy(taskListEntity.getUpdatedBy())
            .updatedAt(taskListEntity.getUpdatedAt())
            .messageParm(taskListEntity.getMessageParm())
            .categoryEn(taskListEntity.getTaskItemTemplate().getCategoryEn())
            .categoryCy(taskListEntity.getTaskItemTemplate().getCategoryCy())
            .role(taskListEntity.getTaskItemTemplate().getRole())
            .taskOrder(taskListEntity.getTaskItemTemplate().getTaskOrder())
            .build();
    }

}
