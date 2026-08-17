package uk.gov.hmcts.reform.civil.enums.dq;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "IncomeType", generate = true)
public enum IncomeTypeLRspec {

    /**
     * Income from job.
     */
    @CCD(label = "Income from your job")
    JOB,
    @CCD(label = "Universal credit")
    UNIVERSAL_CREDIT,
    /**
     * Jobseeker's Allowance (income based).
     */
    @CCD(label = "Jobseeker's Allowance (income based)")
    JOBSEEKER_ALLOWANCE_INCOME,
    /**
     * Jobseeker's Allowance (contribution based).
     */
    @CCD(label = "Jobseeker's Allowance (contribution based)")
    JOBSEEKER_ALLOWANCE_CONTRIBUTION,
    @CCD(label = "Income support")
    INCOME_SUPPORT,
    @CCD(label = "Working Tax Credit")
    WORKING_TAX_CREDIT,
    /**
     * Child Tax Credit.
     */
    @CCD(label = "Child Tax Credit")
    CHILD_TAX,
    @CCD(label = "Child benefit")
    CHILD_BENEFIT,
    @CCD(label = "Council Tax Support")
    COUNCIL_TAX_SUPPORT,
    @CCD(label = "Pension")
    PENSION,
    @CCD(label = "Other")
    OTHER
}
