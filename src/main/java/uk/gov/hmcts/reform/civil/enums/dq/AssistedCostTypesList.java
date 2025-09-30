package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssistedCostTypesList {

    COSTS_IN_CASE("Costs in the case"),
    NO_ORDER_TO_COST("No order as to costs"),
    COSTS_RESERVED("Costs reserved"),
    MAKE_AN_ORDER_FOR_DETAILED_COSTS("Make an Order for detailed costs"),
    BESPOKE_COSTS_ORDER("Bespoke costs order"),

    REFUSED("refused");

    private final String displayedValue;
}
