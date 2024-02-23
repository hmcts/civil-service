package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HwFMoreInfoRequiredDocuments {
    ANY_OTHER_INCOME,
    BANK_STATEMENTS,
    BENEFITS_AND_TAX_CREDITS,
    CHILD_MAINTENANCE,
    INCOME_FROM_SELLING_GOODS,
    PENSIONS,
    PRISONERS_INCOME,
    RENTAL_INCOME,
    WAGE_SLIPS
}
