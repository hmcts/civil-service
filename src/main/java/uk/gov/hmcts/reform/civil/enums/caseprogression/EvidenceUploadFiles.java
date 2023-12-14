package uk.gov.hmcts.reform.civil.enums.caseprogression;

public enum EvidenceUploadFiles {

    DISCLOSURE_LIST("Disclosure List", "Disclosure list"),
    DOCUMENTS_FOR_DISCLOSURE("Document for disclosure", "Disclosure list"),
    EXPERT_REPORT("Expert Report", "Expert evidence"),
    JOINT_STATEMENT("Joint Statement", "Expert evidence"),
    QUESTIONS_FOR_EXPERTS("Expert Questions", "Expert evidence"),
    ANSWERS_FOR_EXPERTS("Expert Answers", "Expert evidence"),
    CASE_SUMMARY("Case Summary", "Trial documents"),
    SKELETON_ARGUMENT("Skeleton Argument", "Trial documents"),
    AUTHORITIES("Authorities", "Trial documents"),
    COSTS("Costs", "Trial documents"),
    DOCUMENTARY("Documentary", "Trial documents"),
    WITNESS_STATEMENT("Witness Statement", "Witness evidence"),
    WITNESS_SUMMARY("Witness Summary", "Witness evidence"),
    NOTICE_OF_INTENTION("Notice of Intention", "Witness evidence"),
    DOCUMENTS_REFERRED("Documents Referred", "Witness evidence");
    String displayName;
    String documentTypeDisplayName;

    EvidenceUploadFiles(String displayName, String documentTypeDisplayName) {
        this.displayName = displayName;
        this.documentTypeDisplayName = documentTypeDisplayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDocumentTypeDisplayName() {
        return documentTypeDisplayName;
    }

}
