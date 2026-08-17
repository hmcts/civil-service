package uk.gov.hmcts.reform.civil.enums.finalorders;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum FinalOrdersDefendantRepresentationList {
    @CCD(label = "Counsel for the defendant")
    COUNSEL_FOR_DEFENDANT,
    @CCD(label = "Solicitor for the defendant")
    SOLICITOR_FOR_DEFENDANT,
    @CCD(label = "Costs draftsman for the defendant")
    COST_DRAFTSMAN_FOR_THE_DEFENDANT,
    @CCD(label = "The defendant in person")
    THE_DEFENDANT_IN_PERSON,
    @CCD(label = "Lay representative for the defendant")
    LAY_REPRESENTATIVE_FOR_THE_DEFENDANT,
    @CCD(label = "Legal Executive")
    LEGAL_EXECUTIVE_FOR_THE_DEFENDANT,
    @CCD(label = "Solicitor's Agent")
    SOLICITORS_AGENT_FOR_THE_DEFENDANT,
    @CCD(label = "The defendant not attending")
    DEFENDANT_NOT_ATTENDING
}
