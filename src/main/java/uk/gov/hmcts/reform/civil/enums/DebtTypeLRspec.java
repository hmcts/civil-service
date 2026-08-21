package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DebtType", generate = true)
@Getter
@RequiredArgsConstructor
public enum DebtTypeLRspec {
    @CCD(label = "Mortgage")
    MORTGAGE("Mortgage"),
    @CCD(label = "Rent")
    RENT("Rent"),
    @CCD(label = "Council tax")
    COUNCIL_TAX("Council tax"),
    @CCD(label = "Gas")
    GAS("Gas"),
    @CCD(label = "Electricity")
    ELECTRICITY("Electricity"),
    @CCD(label = "Water")
    WATER("Water"),
    @CCD(label = "Maintenance payments")
    MAINTENANCE_PAYMENTS("Maintenance payments");

    private final String label;
}
