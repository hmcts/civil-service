package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HwFMoreInfoRequiredDocuments {
    BANK_STATEMENTS,
    WAGE_SLIPS,
    CHILD_MAINTENANCE,
    BENEFITS_AND_TAX_CREDITS,
    PENSIONS,
    RENTAL_INCOME,
    INCOME_FROM_SELLING_GOODS,
    PRISONERS_INCOME,
    ANY_OTHER_INCOME
}
