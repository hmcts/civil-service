package uk.gov.hmcts.reform.civil.ga.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GaFinalOrderSelection {
    ASSISTED_ORDER("Assisted order"),
    FREE_FORM_ORDER("Free form order");

    private final String displayedValue;
}
