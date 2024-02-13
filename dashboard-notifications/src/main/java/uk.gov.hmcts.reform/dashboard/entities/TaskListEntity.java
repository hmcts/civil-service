package uk.gov.hmcts.reform.dashboard.entities;

import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "task_list", schema = "dbs")
public class TaskListEntity implements Serializable {

    private static final long serialVersionUID = 679573393379454443L;

    @Id
    private UUID id;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private TaskItemTemplateEntity taskItemTemplate;

    @Size(max = 256)
    private String reference;

    private int currentStatus;

    private int nextStatus;

    @Size(max = 256)
    private String taskNameEn;

    @Size(max = 512)
    private String hintTextEn;

    @Size(max = 256)
    private String taskNameCy;

    @Size(max = 512)
    private String hintTextCy;

    @NotNull
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @Size(max = 256)
    private String updatedBy;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> messageParm;
}
