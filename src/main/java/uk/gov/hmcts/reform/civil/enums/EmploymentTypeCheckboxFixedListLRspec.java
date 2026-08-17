package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum EmploymentTypeCheckboxFixedListLRspec {
    @CCD(label = "Employed")
    EMPLOYED("Employed"),
    @CCD(label = "Self-employed")
    SELF("Self-employed");

    private final String displayedValue;
}
