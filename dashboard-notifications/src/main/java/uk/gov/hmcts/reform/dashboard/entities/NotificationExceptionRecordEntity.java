package uk.gov.hmcts.reform.dashboard.entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "notification_exception_record", schema = "dbs")
public class NotificationExceptionRecordEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 8313686152850559308L;

    @Id
    @NotNull
    @Schema(name = "id")
    private UUID id;

    @Schema(name = "reference")
    private String reference;

    @Schema(name = "event_id")
    private String eventId;

    @Schema(name = "party_type")
    private String partyType;

    @Schema(name = "retry_count")
    private String retryCount;

    @Schema(name = "created_at")
    private OffsetDateTime createdAt;

    @Schema(name = "updated_on")
    private OffsetDateTime updatedOn;

}
