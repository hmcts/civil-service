package uk.gov.hmcts.reform.civil.enums.finalorders;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum FinalOrdersClaimantRepresentationList {
    @CCD(label = "Counsel for the claimant")
    COUNSEL_FOR_CLAIMANT,
    @CCD(label = "Solicitor for the claimant")
    SOLICITOR_FOR_CLAIMANT,
    @CCD(label = "Costs draftsman for the claimant")
    COST_DRAFTSMAN_FOR_THE_CLAIMANT,
    @CCD(label = "The claimant in person")
    THE_CLAIMANT_IN_PERSON,
    @CCD(label = "Lay representative for the claimant")
    LAY_REPRESENTATIVE_FOR_THE_CLAIMANT,
    @CCD(label = "Legal Executive")
    LEGAL_EXECUTIVE_FOR_THE_CLAIMANT,
    @CCD(label = "Solicitor's Agent")
    SOLICITORS_AGENT_FOR_THE_CLAIMANT,
    @CCD(label = "The claimant not attending")
    CLAIMANT_NOT_ATTENDING
}
