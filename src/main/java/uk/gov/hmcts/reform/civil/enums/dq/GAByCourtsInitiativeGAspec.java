package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAByCourtsInitiativeGAspec {

    OPTION_1("Order on court's own initiative"),
    OPTION_2("Order without notice"),
    OPTION_3("Not applicable");

    private final String displayedValue;
}
