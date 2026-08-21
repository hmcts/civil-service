package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum DisposalHearingFinalDisposalHearingTimeEstimate {
    @CCD(label = "30 minutes")
    THIRTY_MINUTES("30 minutes"),
    @CCD(label = "15 minutes")
    FIFTEEN_MINUTES("15 minutes"),
    @CCD(label = "Other")
    OTHER("Other");

    private final String label;

    DisposalHearingFinalDisposalHearingTimeEstimate(String value) {
        this.label = value;
    }
}
