package uk.gov.hmcts.reform.dashboard.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
@Table(name = "task_item_identifier", schema = "public")
@Entity
public class TaskItemIdentifierEntity {
    @Id
    private UUID id;

    @NotNull
    @ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
    @JoinColumn (name = "task_item_template_id", nullable = false)
    private TaskItemTemplateEntity taskItemTemplate;

    @Size(max = 20)
    @NotNull
    private String caseReference;

}
