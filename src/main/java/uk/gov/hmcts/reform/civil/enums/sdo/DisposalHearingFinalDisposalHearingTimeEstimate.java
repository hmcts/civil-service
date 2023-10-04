package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum DisposalHearingFinalDisposalHearingTimeEstimate {
    THIRTY_MINUTES("30 minutes"),
    FIFTEEN_MINUTES("15 minutes"),
    OTHER("Other");

    private final String label;

    DisposalHearingFinalDisposalHearingTimeEstimate(String value) {
        this.label = value;
    }
}
