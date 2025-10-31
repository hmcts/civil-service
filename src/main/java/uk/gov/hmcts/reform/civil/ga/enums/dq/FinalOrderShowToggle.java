package uk.gov.hmcts.reform.civil.ga.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FinalOrderShowToggle {
    SHOW("Show"),
    HIDE("Hide");
    private final String displayedValue;
}
