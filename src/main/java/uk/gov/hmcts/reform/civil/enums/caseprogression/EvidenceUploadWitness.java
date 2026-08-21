package uk.gov.hmcts.reform.civil.enums.caseprogression;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum EvidenceUploadWitness {

    @CCD(label = "Witness statement")
    WITNESS_STATEMENT,
    @CCD(label = "Witness summary")
    WITNESS_SUMMARY,
    @CCD(label = "Notice of the intention to rely on hearsay evidence")
    NOTICE_OF_INTENTION,
    @CCD(label = "Documents referred to in the statement")
    DOCUMENTS_REFERRED

}
