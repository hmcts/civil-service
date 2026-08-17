package uk.gov.hmcts.reform.civil.enums.caseprogression;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum EvidenceUploadDisclosure {

    @CCD(label = "Disclosure list")
    DISCLOSURE_LIST,
    @CCD(label = "Documents for disclosure")
    DOCUMENTS_FOR_DISCLOSURE

}
