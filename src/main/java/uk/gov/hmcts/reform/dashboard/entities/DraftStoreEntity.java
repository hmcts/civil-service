//package uk.gov.hmcts.reform.dashboard.entities;
//import io.swagger.v3.oas.annotations.media.Schema;
//import java.time.LocalDateTime;
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.type.SqlTypes;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import jakarta.validation.constraints.NotNull;
//import java.io.Serializable;
//import java.time.OffsetDateTime;
//import java.util.HashMap;
//import java.util.UUID;
//
//@Lombok.Data
//@Lombok.NoArgsConstructor
//@Lombok.AllArgsConstructor
//@Entity
//@Table(name = “draft_store”, schema = "dbs”)
//public class DraftStoreEntity implements Serializable {
//
//    private static final long serialVersionUID = ?;
//
//    @Id
//    @NotNull
//    @Schema(name = “draft_id”)
//    private UUID draftId;
//
//    @NotNull
//    @Size(max = 200)
//    @Schema(name = “user_id”)
//    private String userId;
//
//    @Size(max = 200)
//    @Schema(name = “case_id”)
//    private String caseId;
//
//    @NotNull
//    @ManyToOne(cascade = CascadeType.REFRESH)
//    @JoinColumn(name = “draft_type_id”)
//    @Schema(name = “draft_type_id”)
//    private DraftTypeEntity draftTypeId;
//
//    @Size(max = 200)
//    @NotNull
//    @Schema(name = “draft_claim”)
//    private String draftClaim;
//
//    @NotNull
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(columnDefinition = “jsonb”)
//    @Schema(name = “payload”)
//    private String payload;
//
//    @NotNull
//    @Schema(name = “draft_claim_created_at”)
//    private OffsetDateTime draftClaimCreatedAt;
//
//    @NotNull
//    @Schema(name = “created_at”)
//    private OffsetDateTime createdAt;
//
//    @NotNull
//    @Schema(name = “updated_at”)
//    private OffsetDateTime updatedAt;
//
//    @NotNull
//    @Schema(name = “expires_at”)
//    private OffsetDateTime expiresAt;
