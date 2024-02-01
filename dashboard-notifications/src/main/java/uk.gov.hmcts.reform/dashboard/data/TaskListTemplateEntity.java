package uk.gov.hmcts.reform.dashboard.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "task_list_template", schema = "public")
public class TaskListTemplateEntity {
    @Id

    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 256)
    @Column(name = "title_en", length = 256)
    private String titleEn;

    @Size(max = 512)
    @Column(name = "content_en", length = 512)
    private String contentEn;

    @Size(max = 256)
    @Column(name = "title_cy", length = 256)
    private String titleCy;

    @Size(max = 512)
    @Column(name = "content_cy", length = 512)
    private String contentCy;

    @Size(max = 256)
    @Column(name = "reference", length = 256)
    private String reference;

    @Size(max = 256)
    @Column(name = "task_status_sequence", length = 256)
    private String taskStatusSequence;

    @Size(max = 256)
    @Column(name = "role", length = 256)
    private String role;

    @Column(name = "order_by")
    private Long orderBy;

    @Size(max = 256)
    @Column(name = "category_en", length = 256)
    private String categoryEn;

    @Size(max = 256)
    @Column(name = "category_cy", length = 256)
    private String categoryCy;

    @NotNull
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

}
