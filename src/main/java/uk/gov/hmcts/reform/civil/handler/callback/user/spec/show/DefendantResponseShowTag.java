package uk.gov.hmcts.reform.civil.handler.callback.user.spec.show;

/**
 * Until front can manage and and or operators in the same showCondition, we need these classes to deal with complex
 * conditions.
 * We can use different enums to keep the different flags easily classified, while using a single field to hold
 * their values.
 */
public enum DefendantResponseShowTag {
    CAN_ANSWER_RESPONDENT_1,
    CAN_ANSWER_RESPONDENT_2,
    ONLY_RESPONDENT_1_DISPUTES,
    ONLY_RESPONDENT_2_DISPUTES,
    BOTH_RESPONDENTS_DISPUTE,
    SOMEONE_DISPUTES,
    CURRENT_ADMITS_PART_OR_FULL,
    TIMELINE_UPLOAD,
    TIMELINE_MANUALLY,
    RESPONDENT_1_PAID_LESS,
    RESPONDENT_2_PAID_LESS,
    /**
     * pageId WhenWillClaimBePaid must be shown.
     */
    WHEN_WILL_CLAIM_BE_PAID,
    /**
     * when r1 chose admits part or admits full.
     */
    RESPONDENT_1_ADMITS_PART_OR_FULL,
    RESPONDENT_2_ADMITS_PART_OR_FULL,
    NEED_FINANCIAL_DETAILS_1,
    NEED_FINANCIAL_DETAILS_2,
    WHY_1_DOES_NOT_PAY_IMMEDIATELY,
    WHY_2_DOES_NOT_PAY_IMMEDIATELY,
    REPAYMENT_PLAN_2,
    MEDIATION,
    VULNERABILITY,
    CLAIMANT_MEDIATION_ONE_V_ONE,
    CLAIMANT_MEDIATION_ADMIT_PAID_ONE_V_ONE,
    CLAIMANT_MEDIATION_ONE_V_TWO,
    CLAIMANT_MEDIATION_TWO_V_ONE
}
