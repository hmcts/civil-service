package uk.gov.hmcts.reform.civil.model.citizenui;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "StaffDocumentType", generate = true)
public enum ManageDocumentType {
    @CCD(label = "N9a (Paper Admission - Full or Part)")
    N9A_PAPER_ADMISSION_FULL_OR_PART,
    @CCD(label = "N9b (Paper defence/Counterclaim)")
    N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
    @CCD(label = "N9 (Request more time)")
    N9_REQUEST_MORE_TIME,
    @CCD(label = "Other")
    OTHER,
    @CCD(label = "Mediation Agreement")
    MEDIATION_AGREEMENT
}
