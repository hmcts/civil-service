package uk.gov.hmcts.reform.civil.enums.caseprogression;

public enum BundleFileNameList {
    CASE_SUMMARY_FILE_DISPLAY_NAME("%s Case summary %s"),
    CHRONOLOGY_FILE_DISPLAY_NAME("%s Chronology %s"),
    TRIAL_TIMETABLE_FILE_DISPLAY_NAME("%s Trial Timetable %s"),
    CLAIM_FORM("Claim Form %s"),
    PARTICULARS_OF_CLAIM("Particulars of Claim"),
    DEFENCE("%s Defence %s"),
    CL_REPLY("CL's reply %s"),
    DF2_DEFENCE("DF 2 Defence"),
    REPLY_TO_PART_18("%s reply to part 18 request %s"),
    DIRECTIONS_QUESTIONNAIRE("Directions Questionnaire"),
    CL_SCHEDULE_OF_LOSS("CL's schedule of loss"),
    DF1_COUNTER_SCHEDULE_OF_LOSS("DF 1 counter schedule of loss"),
    DF2_COUNTER_SCHEDULE_OF_LOSS("DF 2 counter schedule of loss"),
    DIRECTIONS_ORDER("Directions Order %s"),
    ORDERS_TO_EXTEND_TIME("Order to Extend Time"),
    ORDERS_FOR_SUMMARY_JUDGEMENT("Orders for summary judgement"),
    ORDER("Order"),
    WITNESS_STATEMENT_DISPLAY_NAME("%s - Statement %s"),
    WITNESS_STATEMENT_OTHER_DISPLAY_NAME("Witness Statement %s"),
    WITNESS_STATEMENT("Witness Statement"),
    WITNESS_SUMMARY("Witness Summary %s %s"),
    HEARSAY_NOTICE("Hearsay notice %s"),
    NOTICE_TO_ADMIT_FACTS("Notice to admit facts %s %s"),
    DF_RESPONSE("DF Response %s"),
    CL_1_EXPERTS("CL 1 Experts"),
    EXPERT_EVIDENCE("Expert Evidence %s %s"),
    QUESTIONS_TO("Questions to %s %s"),
    REPLIES_FROM("Replies from %s %s"),
    JOINT_STATEMENTS_OF_EXPERTS("Joint statements of experts/Single joint expert report"),
    CL1_DOCUMENTS_THEY_INTEND_TO_RELY_ON("CL 1 Documents they intend to rely on"),
    INVOICE("Invoice"),
    CL2_DOCUMENTS_THEY_INTEND_TO_RELY_ON("CL 2 Documents they intend to rely on"),
    DF1_DOCUMENTS_THEY_INTEND_TO_RELY_ON("DF 1 Documents they intend to rely on"),
    DF2_DOCUMENTS_THEY_INTEND_TO_RELY_ON("DF 2 Documents they intend to rely on"),
    CL1_COSTS_BUDGET("CL 1 Costs Budget"),
    CL2_COSTS_BUDGET("CL 2 Costs Budget"),
    DF1_COSTS_BUDGET("DF 1 Costs Budget"),
    DF2_COSTS_BUDGET("DF 2 Costs Budget");

    String displayName;

    public String getDisplayName() {
        return displayName;
    }

    BundleFileNameList(String displayName) {
        this.displayName = displayName;
    }
}
