package uk.gov.hmcts.reform.civil.enums.caseprogression;

import static uk.gov.hmcts.reform.civil.constants.EnumDuplicateLiteralConstants.TRIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.constants.EnumDuplicateLiteralConstants.WITNESS_EVIDENCE;

public enum EvidenceUploadType {

    DISCLOSURE_LIST("Disclosure list", "%s - Disclosure list"),
    DOCUMENTS_FOR_DISCLOSURE("Document for disclosure", "%s - Documents for disclosure"),
    EXPERT_REPORT("Experts report", "%s - Expert's report"),
    PRE_TRIAL_SUMMARY("Pre Trial Summary", "%s - Case Summary"),
    JOINT_STATEMENT("Joint report", "%s - Joint Statement of Experts / Single Joint Expert Report"),
    QUESTIONS_FOR_EXPERTS("Expert Questions", "%s - Questions for other party's expert or joint experts"),
    ANSWERS_FOR_EXPERTS("Expert Answers", "%s - Answer to questions asked"),
    CASE_SUMMARY("Case Summary", TRIAL_DOCUMENTS),
    SKELETON_ARGUMENT("Skeleton Argument", TRIAL_DOCUMENTS),
    AUTHORITIES("Authorities", "%s - Authorities"),
    COSTS("Costs", "%s - Costs"),
    TRIAL_CORRESPONDENCE("Documentary Evidence", "%s - Documentary evidence for trial"),
    TRIAL_SKELETON("Trial Skeleton", "%s - Skeleton argument"),
    DOCUMENTARY("Documentary", TRIAL_DOCUMENTS),
    WITNESS_STATEMENT("Witness Statement of", "%s - Witness statement"),
    WITNESS_SUMMARY("Witness Summary of", "%s - Witness summary"),
    WITNESS_HEARSAY("Hearsay evidence", "%s - Notice of the intention to rely on hearsay evidence"),
    WITNESS_REFERRED(" referred to in the statement of ", "%s - Documents referred to in the statement"),
    NOTICE_OF_INTENTION("Notice of Intention", WITNESS_EVIDENCE),
    DOCUMENTS_REFERRED("Documents Referred", WITNESS_EVIDENCE),
    BUNDLE_EVIDENCE("Bundle evidence", "%s - Bundle");

    String documentTypeDisplayName;
    String notifictationTextRegEx;

    EvidenceUploadType(String documentTypeDisplayName, String notifictationTextRegEx) {
        this.documentTypeDisplayName = documentTypeDisplayName;
        this.notifictationTextRegEx = notifictationTextRegEx;
    }

    public String getNotifictationTextRegEx() {
        return notifictationTextRegEx;
    }

    public String getDocumentTypeDisplayName() {
        return documentTypeDisplayName;
    }
}
