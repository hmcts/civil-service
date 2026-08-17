package uk.gov.hmcts.reform.civil.model.mediation;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum MediationDocumentsType {
    @CCD(label = "Non-attendance statement")
    NON_ATTENDANCE_STATEMENT,
    @CCD(label = "Documents referred to in the statement")
    REFERRED_DOCUMENTS
}
