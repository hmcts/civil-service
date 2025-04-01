package uk.gov.hmcts.reform.dashboard.entities;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "exception_record", schema = "dbs")
@TypeDefs({
    @TypeDef(
        name = "list-array",
        typeClass = ListArrayType.class
        )
})
public class ExceptionRecordEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 8313686152850559308L;

    @Id
    String idempotencyKey;

    @Schema(name = "reference")
    private String reference;

    @Schema(name = "task_id")
    private String taskId;

    @Schema(name = "successful_actions")
    @Type(type = "list-array")
    @Column(
        name = "successful_actions",
        columnDefinition = "text[]"
    )
    @Builder.Default
    private List<String> successfulActions = new ArrayList<>();

    @Schema(name = "retry_count")
    private int remainingRetries;

    @Schema(name = "created_at")
    private OffsetDateTime createdAt;

    @Schema(name = "updated_on")
    private OffsetDateTime updatedOn;
}
