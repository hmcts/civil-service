package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FinalOrderConsideredToggle {
    CONSIDERED("The judge considered the papers"),
    NOT_CONSIDERED("Not considered");
    private final String displayedValue;
}
