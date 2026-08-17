package uk.gov.hmcts.reform.civil.enums.caseprogression;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum EvidenceUploadTrial {

    @CCD(label = "Case Summary")
    CASE_SUMMARY,
    @CCD(label = "Skeleton argument")
    SKELETON_ARGUMENT,
    @CCD(label = "Authorities")
    AUTHORITIES,
    @CCD(label = "Costs")
    COSTS,
    @CCD(label = "Documentary evidence for trial")
    DOCUMENTARY
}
