package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentTypeCheckboxFixedListLRspec {
    EMPLOYED("Employed"),
    SELF("Self-employed");

    private final String displayedValue;
}
