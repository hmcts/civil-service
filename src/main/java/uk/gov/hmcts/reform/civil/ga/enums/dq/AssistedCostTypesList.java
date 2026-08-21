package uk.gov.hmcts.reform.civil.ga.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum AssistedCostTypesList {

    COSTS_IN_CASE("Costs in the case"),
    @CCD(label = "No order as to costs")
    NO_ORDER_TO_COST("No order as to costs"),
    @CCD(label = "Costs reserved (not where multiple claimants/defendants: use bespoke order for those cases)")
    COSTS_RESERVED("Costs reserved"),
    @CCD(
            label = "Make an order for detailed/summary costs (not where multiple claimants/defendants: use bespoke order for those cases)"
    )
    MAKE_AN_ORDER_FOR_DETAILED_COSTS("Make an Order for detailed costs"),
    @CCD(label = "Bespoke costs order (free text)")
    BESPOKE_COSTS_ORDER("Bespoke costs order"),

    REFUSED("refused");

    private final String displayedValue;
}
