package uk.gov.hmcts.reform.dashboard.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.time.OffsetDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Immutable
@Table(name = "task_item_template", schema = "dbs")
public class TaskItemTemplateEntity implements Serializable {

    private static final long serialVersionUID = -2461367611245226407L;

    @Id
    @Schema(name = "id")
    private Long id;

    @Size(max = 256)
    @Schema(name = "task_name_en")
    private String taskNameEn;

    @Size(max = 512)
    @Schema(name = "hint_text_en")
    private String hintTextEn;

    @Size(max = 256)
    @Schema(name = "category_en")
    private String categoryEn;

    @Size(max = 256)
    @Schema(name = "task_name_cy")
    private String taskNameCy;

    @Size(max = 512)
    @Schema(name = "hint_text_cy")
    private String hintTextCy;

    @Size(max = 256)
    @Schema(name = "category_cy")
    private String categoryCy;

    @Size(max = 256)
    @Schema(name = "scenario_name")
    private String scenarioName;

    @Size(max = 256)
    @Schema(name = "template_name")
    private String templateName;

    @Size(max = 256)
    @Schema(name = "role")
    private String role;

    @Schema(name = "task_order")
    private int taskOrder;

    @NotNull
    @Schema(name = "created_at")
    private OffsetDateTime createdAt;

    @Type(type = "com.vladmihalcea.hibernate.type.array.IntArrayType")
    @Schema(name = "task_status_sequence")
    private int[] taskStatusSequence;
}
