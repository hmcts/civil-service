package uk.gov.hmcts.reform.dashboard.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Table(name = "task_item_template", schema = "public")
@Entity
public class TaskItemTemplateEntity {
    @Id
    private Long id;

    @Size(max = 256)
    private String titleEn;

    @Size(max = 512)
    private String contentEn;

    @Size(max = 256)
    private String categoryEn;

    @Size(max = 256)
    private String titleCy;

    @Size(max = 512)
    private String contentCy;

    @Size(max = 256)
    private String categoryCy;

    @Size(max = 256)
    private String name;

    @Size(max = 256)
    private String role;

    private Short taskOrder;

    @NotNull
    @javax.persistence.Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "taskItemTemplate")
    private Set<TaskListEntity> taskLists = new LinkedHashSet<>();

    private Object taskStatusSequence;

}
