package uk.gov.hmcts.reform.dashboard.entities;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "id")
    private UUID id;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    @Schema(name = "task_item_template_id")
    private TaskItemTemplateEntity taskItemTemplate;

    @Size(max = 256)
    @Schema(name = "reference")
    private String reference;

    @Schema(name = "current_status")
    private int currentStatus;

    @Schema(name = "next_status")
    private int nextStatus;

    @Size(max = 256)
    @Schema(name = "task_name_en")
    private String taskNameEn;

    @Size(max = 512)
    @Schema(name = "hint_text_en")
    private String hintTextEn;

    @Size(max = 256)
    @Schema(name = "task_name_cy")
    private String taskNameCy;

    @Size(max = 512)
    @Schema(name = "hint_text_cy")
    private String hintTextCy;

    @NotNull
    @Schema(name = "created_at")
    private OffsetDateTime createdAt;

    @Schema(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Size(max = 256)
    @Schema(name = "updated_by")
    private String updatedBy;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Schema(name = "message_params")
    private Map<String, Object> messageParams;
}
