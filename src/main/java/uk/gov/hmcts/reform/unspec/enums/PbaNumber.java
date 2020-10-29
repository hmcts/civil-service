package uk.gov.hmcts.reform.unspec.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PbaNumber {
    PBA0077597(AccountType.ACTIVE),
    PBA0078094(AccountType.ON_HOLD),
    PBA0079005(AccountType.DELETED);

    private final AccountType accountType;

    public enum AccountType {
        ACTIVE,
        ON_HOLD,
        DELETED
    }
}
