package uk.gov.hmcts.reform.draftstore;

import lombok.Getter;

@Getter
public enum DraftType {
    DRAFT_CLAIM(30, 7, 180);

    private final long retentionDays;
    private final long paymentRetentionDays;
    private final long legacyRetentionDays;

    DraftType(long retentionDays, long paymentRetentionDays, long legacyRetentionDays) {
        this.retentionDays = retentionDays;
        this.paymentRetentionDays = paymentRetentionDays;
        this.legacyRetentionDays = legacyRetentionDays;
    }
}
