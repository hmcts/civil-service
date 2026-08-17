package uk.gov.hmcts.reform.civil.enums.finalorders;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum AppealList {
    @CCD(label = "Claimant's")
    CLAIMANT,
    @CCD(label = "Defendant's")
    DEFENDANT,
    @CCD(label = "Other")
    OTHER
}
