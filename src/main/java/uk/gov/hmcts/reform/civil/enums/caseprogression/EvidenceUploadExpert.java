package uk.gov.hmcts.reform.civil.enums.caseprogression;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum EvidenceUploadExpert {

    @CCD(label = "Expert's report")
    EXPERT_REPORT,
    @CCD(label = "Joint Statement of Experts / Single Joint Expert Report")
    JOINT_STATEMENT,
    @CCD(label = "Questions asked of other party expert")
    QUESTIONS_FOR_EXPERTS,
    @CCD(label = "Answer to questions asked")
    ANSWERS_FOR_EXPERTS

}
