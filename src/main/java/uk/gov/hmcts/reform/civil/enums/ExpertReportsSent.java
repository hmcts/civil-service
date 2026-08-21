package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ExpertReportsSent {
    @CCD(label = "Yes")
    YES("Yes"),
    @CCD(label = "No")
    NO("No"),
    @CCD(label = "Not yet obtained")
    NOT_OBTAINED("Not yet obtained");

    private final String displayedValue;
}
