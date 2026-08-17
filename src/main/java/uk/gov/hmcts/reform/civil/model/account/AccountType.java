package uk.gov.hmcts.reform.civil.model.account;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum AccountType {
    /**
     * current account.
     */
    @CCD(label = "Current account")
    CURRENT,

    /**
     * savings account.
     */
    @CCD(label = "Savings account")
    SAVINGS,

    /**
     * individual savings account.
     */
    ISA,

    /**
     * other kind of account.
     */
    @CCD(label = "Other")
    OTHER
}
