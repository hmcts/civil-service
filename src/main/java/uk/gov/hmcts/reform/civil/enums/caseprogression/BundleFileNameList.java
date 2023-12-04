package uk.gov.hmcts.reform.civil.enums.caseprogression;

public enum BundleFileNameList {
    CASE_SUMMARY_FILE_DISPLAY_NAME("%s Case summary %s"),
    CHRONOLOGY_FILE_DISPLAY_NAME("%s Chronology %s"),
    TRIAL_TIMETABLE_FILE_DISPLAY_NAME("%s Trial Timetable %s"),
    SCHEDULE_OF_LOSS_FILE_DISPLAY_NAME("%s schedule of loss %s"),
    CLAIM_FORM("Claim Form %s"),
    DEFENCE("%s Defence %s"),
    CL_REPLY("CL's reply %s"),
    DF2_DEFENCE("DF 2 Defence"),
    REPLY_TO_PART_18("%s reply to part 18 request %s"),
    DIRECTIONS_QUESTIONNAIRE("Directions Questionnaire %s"),
    DIRECTIONS_ORDER("Directions Order %s"),
    ORDER("Order %s"),
    WITNESS_STATEMENT_DISPLAY_NAME("%s - Statement %s"),
    WITNESS_STATEMENT_OTHER_DISPLAY_NAME("Witness Statement %s %s %s"),
    WITNESS_STATEMENT("Witness Statement"),
    WITNESS_SUMMARY("Witness Summary %s %s"),
    DOC_REFERRED_TO("%s referred to in the statement of %s %s"),
    HEARSAY_NOTICE("Hearsay notice %s %s"),
    NOTICE_TO_ADMIT_FACTS("Notice to admit facts %s %s"),
    DF_RESPONSE("DF Response %s"),
    CL_1_EXPERTS("CL 1 Experts"),
    EXPERT_EVIDENCE("Expert Evidence %s %s %s"),
    QUESTIONS_TO("Questions to %s %s"),
    REPLIES_FROM("Replies from %s %s"),
    JOINT_STATEMENTS_OF_EXPERTS("Joint statement of experts %s %s %s"),
    SKELETON_ARGUMENT("%s Skeleton argument %s"),
    PARTICULARS_OF_CLAIM("Particulars Of Claim %s");
    String displayName;

    public String getDisplayName() {
        return displayName;
    }

    BundleFileNameList(String displayName) {
        this.displayName = displayName;
    }
}
