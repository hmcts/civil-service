package uk.gov.hmcts.reform.civil.model.interestcalc;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "InterestClaimFrom", generate = true)
public enum InterestClaimFromType {
    @CCD(
            label = "The date you submit the claim. \n\n\n The interest will then be calculated up until the claim is settled or a judgment has been made."
    )
    FROM_CLAIM_SUBMIT_DATE,
    @CCD(
            label = "A specific date. \n\n\n For example, the date an invoice was overdue, or the date that you told someone they owed you money."
    )
    FROM_A_SPECIFIC_DATE
}

