package uk.gov.hmcts.reform.civil.enums.finalorders;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FinalOrdersRepresentationList", generate = true)
public enum FinalOrderRepresentationList {
    @CCD(label = "Claimant(s) and defendant(s)")
    CLAIMANT_AND_DEFENDANT,
    @CCD(label = "Other representation (free text)")
    OTHER_REPRESENTATION
}
