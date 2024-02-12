package uk.gov.hmcts.reform.dashboard.entities;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.dashboard.utils.JsonDataConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@lombok.Data
@lombok.Builder
@AllArgsConstructor
@lombok.NoArgsConstructor
@Entity
@Table(name = "task_list", schema = "dbs")
public class TaskListEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 679573393379454443L;

    @Id
    private UUID id;
    @NotNull
    @ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_item_template_id", nullable = false)
    private TaskItemTemplateEntity taskItemTemplate;
    @Size(max = 256)
    private String reference;
    private int currentStatus;
    private int nextStatus;
    @Size(max = 512)
    private String taskItemEn;
    @Size(max = 512)
    private String taskItemCy;
    @NotNull
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Size(max = 256)
    private String updatedBy;
    @Column(name = "message_parm", columnDefinition = "jsonb(0, 0)")
    @Convert(converter = JsonDataConverter.class)
    private JsonNode messageParm;
}
