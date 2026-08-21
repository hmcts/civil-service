package uk.gov.hmcts.reform.draftstore;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public enum DraftType {

    DRAFT_CLAIM(1, 180L);

    private static final long DEFAULT_RETENTION_DAYS = 30L;

    private final int id;
    private final long retentionDays;

    DraftType(int id, Long retentionDays) {
        this.id = id;
        this.retentionDays = resolveRetentionDays(retentionDays);
    }

    public OffsetDateTime calculateExpiry(OffsetDateTime createdAt) {
        return createdAt.plusDays(retentionDays);
    }

    static long resolveRetentionDays(Long retentionDays) {
        if (retentionDays == null) {
            return DEFAULT_RETENTION_DAYS;
        }
        if (retentionDays <= 0) {
            throw new IllegalArgumentException("retentionDays must be positive");
        }
        return retentionDays;
    }
}
