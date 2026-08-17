package uk.gov.hmcts.reform.civil.enums.dq;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ExpenseType", generate = true)
public enum ExpenseTypeLRspec {

    @CCD(label = "Mortgage")
    MORTGAGE,
    @CCD(label = "Rent")
    RENT,
    @CCD(label = "Council tax")
    COUNCIL_TAX,
    @CCD(label = "Gas")
    GAS,
    @CCD(label = "Electricity")
    ELECTRICITY,
    @CCD(label = "Water")
    WATER,
    /**
     * Travel (Work or school).
     */
    @CCD(label = "Travel (Work or school)")
    TRAVEL,
    /**
     * School costs (include clothing).
     */
    @CCD(label = "School costs (include clothing)")
    SCHOOL,
    /**
     * Food and housekeeping.
     */
    @CCD(label = "Food and housekeeping")
    FOOD,
    /**
     * TV and broadband.
     */
    @CCD(label = "TV and broadband")
    TV,
    @CCD(label = "Hire purchase")
    HIRE_PURCHASE,
    @CCD(label = "Mobile phone")
    MOBILE_PHONE,
    /**
     * Maintenance payments.
     */
    @CCD(label = "Maintenance payments")
    MAINTENANCE,
    @CCD(label = "Other")
    OTHER
}
