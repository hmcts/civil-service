package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DebtTypeLRspec {
    MORTGAGE("Mortgage"),
    RENT("Rent"),
    COUNCIL_TAX("Council tax"),
    GAS("Gas"),
    ELECTRICITY("Electricity"),
    WATER("Water"),
    MAINTENANCE_PAYMENTS("Maintenance payments");

    private final String label;
}
