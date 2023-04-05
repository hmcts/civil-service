package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneralApplicationTypes {
    STRIKE_OUT("Strike Out"),
    SUMMARY_JUDGEMENT("Summary Judgement"),
    STAY_THE_CLAIM("Stay the claim"),
    EXTEND_TIME("Extend Time"),
    AMEND_A_STMT_OF_CASE("Amend a statement of case"),
    RELIEF_FROM_SANCTIONS("Relief from sanctions"),
    SET_ASIDE_JUDGEMENT("Application to Set Aside Judgement"),
    VARY_ORDER("Application to Vary Order"),
    ADJOURN_VACATE_HEARING("Application to adjourn/vacate a hearing"),
    UNLESS_ORDER("Application for Unless Order"),
    OTHER("Other"),
    VARY_JUDGEMENT("Application to Vary Judgement");

    private final String displayedValue;
}
