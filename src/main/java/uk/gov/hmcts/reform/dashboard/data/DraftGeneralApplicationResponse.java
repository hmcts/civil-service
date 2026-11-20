package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftGeneralApplicationResponse {

    private UUID draftId;
    private String caseId;
    private String applicationId;
    private String draftType;
    private Map<String, Object> payload;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static DraftGeneralApplicationResponse from(DraftStoreEntity draft) {
        return new DraftGeneralApplicationResponse(
            draft.getId(),
            draft.getCaseId(),
            draft.getApplicationId(),
            draft.getDraftType().name(),
            new HashMap<>(draft.getPayload()),
            draft.getExpiresAt(),
            draft.getCreatedAt(),
            draft.getUpdatedAt()
        );
    }
}
