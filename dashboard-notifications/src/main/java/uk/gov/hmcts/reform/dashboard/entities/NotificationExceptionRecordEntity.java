package uk.gov.hmcts.reform.dashboard.entities;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "notification_exception_record", schema = "dbs")
@TypeDefs({
    @TypeDef(
        name = "string-array",
        typeClass = StringArrayType.class
        )
})
public class NotificationExceptionRecordEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 8313686152850559308L;

    @EmbeddedId
    NotificationExceptionId notificationExceptionId;

    @Schema(name = "party_type")
    private String partyType;

    @Schema(name = "successful_actions")
    @Type(type = "string-array")
    @Column(
        name = "successful_actions",
        columnDefinition = "text[]"
    )
    private List<String> successfulActions;

    @Schema(name = "retry_count")
    private int retryCount;

    @Schema(name = "created_at")
    private OffsetDateTime createdAt;

    @Schema(name = "updated_on")
    private OffsetDateTime updatedOn;
}
