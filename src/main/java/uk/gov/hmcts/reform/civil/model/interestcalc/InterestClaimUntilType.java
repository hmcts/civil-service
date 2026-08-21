package uk.gov.hmcts.reform.civil.model.interestcalc;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "InterestClaimUntil", generate = true)
public enum InterestClaimUntilType {
    @CCD(label = "When you submit the claim.")
    UNTIL_CLAIM_SUBMIT_DATE,
    @CCD(label = "Until the claim is settled or judgment made.")
    UNTIL_SETTLED_OR_JUDGEMENT_MADE
}
