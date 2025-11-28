package uk.gov.hmcts.reform.dashboard.entities;

import com.vladmihalcea.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Type;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashMap;
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
    @NotNull
    @Schema(name = "id")
    private UUID id;

    @NotNull
    @ManyToOne(cascade = CascadeType.REFRESH)
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

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Schema(name = "message_params")
    private HashMap<String, Object> messageParams;
}
