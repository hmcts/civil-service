package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private int currentStatus;

    private int nextStatus;

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

}
