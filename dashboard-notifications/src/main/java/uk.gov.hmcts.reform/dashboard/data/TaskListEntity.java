package uk.gov.hmcts.reform.dashboard.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Table(name = "task_list", schema = "public")
@Entity
public class TaskListEntity {
    @Id
    @NotNull
    private UUID id;

    private Integer currentStatus;

    private Integer nextStatus;

    @Size(max = 512)
    private String taskItemEn;

    @Size(max = 512)
    private String taskItemCy;

    @Size(max = 256)
    private String messageParm;

    @NotNull
    private Instant createdAt;

    private Instant modifiedAt;

    @Size(max = 256)
    private String createdBy;

    @Size(max = 256)
    private String modifiedBy;

}
