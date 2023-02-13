package uk.gov.hmcts.reform.civil.enums.caseprogression;

public enum EvidenceUploadFiles {

    DISCLOSURE_LIST("Disclosure List"),
    DOCUMENTS_FOR_DISCLOSURE("Document for disclosure"),
    EXPERT_REPORT("Expert Report"),
    JOINT_STATEMENT("Joint Statement"),
    QUESTIONS_FOR_EXPERTS("Expert Questions"),
    ANSWERS_FOR_EXPERTS("Expert Answers"),
    CASE_SUMMARY("Case Summary"),
    SKELETON_ARGUMENT("Skeleton Argument"),
    AUTHORITIES("Authorities"),
    COSTS("Costs"),
    DOCUMENTARY("Documentary"),
    WITNESS_STATEMENT("Witness Statement"),
    WITNESS_SUMMARY("Witness Summary"),
    NOTICE_OF_INTENTION("Notice of Intention"),
    DOCUMENTS_REFERRED("Documents Referred");
    String displayName;

    EvidenceUploadFiles(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
