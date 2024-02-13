package uk.gov.hmcts.reform.dashboard.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String updatedBy;

    private JsonNode messageParm;

    private String categoryEn;

    private String categoryCy;

    private String role;

    private int taskOrder;

}
