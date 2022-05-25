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
    RESPONDENT_1_PAID_LESS,
    RESPONDENT_2_PAID_LESS
}
