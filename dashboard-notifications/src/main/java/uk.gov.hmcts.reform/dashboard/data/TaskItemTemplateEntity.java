package uk.gov.hmcts.reform.dashboard.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_item_template_id_seq")
    @SequenceGenerator(name = "task_item_template_id_seq", sequenceName = "task_item_template_id_seq", allocationSize = 1)
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

    private Integer taskOrder;

    @NotNull
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "taskItemTemplate")
    private Set<TaskItemIdentifierEntity> taskItemIdentifiers = new LinkedHashSet<>();

    private Object taskStatusSequence;

}
