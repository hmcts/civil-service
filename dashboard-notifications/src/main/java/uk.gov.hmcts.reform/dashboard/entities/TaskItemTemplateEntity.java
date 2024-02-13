package uk.gov.hmcts.reform.dashboard.entities;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "task_item_template", schema = "dbs")
public class TaskItemTemplateEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -2461367611245226407L;

    @Id
    private Long id;

    @Size(max = 256)
    private String taskNameEn;

    @Size(max = 512)
    private String hintTextEn;

    @Size(max = 256)
    private String categoryEn;

    @Size(max = 256)
    private String taskNameCy;

    @Size(max = 512)
    private String hintTextCy;

    @Size(max = 256)
    private String categoryCy;

    @Size(max = 256)
    private String name;

    @Size(max = 256)
    private String role;

    private int taskOrder;

    @javax.persistence.Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Type(type = "com.vladmihalcea.hibernate.type.array.IntArrayType")
    private int[] taskStatusSequence;
}
