package uk.gov.hmcts.reform.civil.enums.cosc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum CoscApplicationStatus {
    @CCD(label = "Active")
    ACTIVE("Active"),
    @CCD(label = "Processed")
    PROCESSED("Processed"),
    @CCD(label = "Inactive")
    INACTIVE("Inactive");

    private final String status;
}
