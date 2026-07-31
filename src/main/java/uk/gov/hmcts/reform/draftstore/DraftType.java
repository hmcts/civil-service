package uk.gov.hmcts.reform.draftstore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@RequiredArgsConstructor
public enum DraftType {

    DRAFT_CLAIM(1, 180),
    UNDEFINED_DRAFT(2, 30);

    private final int id;
    private final long retentionDays;

    public OffsetDateTime calculateExpiry(OffsetDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        return createdAt.plusDays(retentionDays);
    }

    public static DraftType getExpiryOrDefault(Integer draftTypeId) {
        if (draftTypeId == null) {
            return UNDEFINED_DRAFT;
        }
        for (DraftType type : values()) {
            if (type.getId() == draftTypeId) {
                return type;
            }
        }
        return UNDEFINED_DRAFT;
    }
}
