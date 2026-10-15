package uk.gov.hmcts.reform.draftstore;

import lombok.Getter;

@Getter
public enum DraftType {
    DRAFT_CLAIM(30, 7);

    private final long retentionDays;
    private final long paymentRetentionDays;

    DraftType(long retentionDays, long paymentRetentionDays) {
        this.retentionDays = retentionDays;
        this.paymentRetentionDays = paymentRetentionDays;
    }
}
