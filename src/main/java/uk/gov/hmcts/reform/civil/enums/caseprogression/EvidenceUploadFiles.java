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

    private static final String WITNESS_EVIDENCE = "Witness evidence";
    private static final String DISCLOSURE_LIST_TYPE = "Disclosure list";
    private static final String TRIAL_DOCUMENTS = "Trial documents";
    private static final String WITNESS_EVIDENCE_TYPE = "Witness evidence";

    String displayName;
    String documentTypeDisplayName;

    EvidenceUploadFiles(String displayName) {
        this.displayName = displayName;
        this.documentTypeDisplayName = determineDocumentType(displayName);
    }

    private String determineDocumentType(String displayName) {
        switch (displayName) {
            case "Disclosure List":
            case "Document for disclosure":
                return DISCLOSURE_LIST_TYPE;
            case "Case Summary":
            case "Skeleton Argument":
            case "Authorities":
            case "Costs":
            case "Documentary":
                return TRIAL_DOCUMENTS;
            case "Witness Statement":
            case "Witness Summary":
            case "Notice of Intention":
            case "Documents Referred":
                return WITNESS_EVIDENCE_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported display name: " + displayName);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDocumentTypeDisplayName() {
        return documentTypeDisplayName;
    }
}
