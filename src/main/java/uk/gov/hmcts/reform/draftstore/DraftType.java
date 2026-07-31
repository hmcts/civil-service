package uk.gov.hmcts.reform.draftstore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@RequiredArgsConstructor
public enum DraftType {

    DRAFT_CLAIM(1, 180);

    private final int id;
    private final long retentionDays;

    public OffsetDateTime calculateExpiry(OffsetDateTime createdAt) {
        return createdAt.plusDays(retentionDays);
    }
}
