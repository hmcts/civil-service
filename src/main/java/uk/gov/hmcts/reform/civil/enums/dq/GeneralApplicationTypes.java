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
    UNLESS_ORDER("Unless order"),
    SET_ASIDE_JUDGEMENT("Set aside judgement"),
    VARY_ORDER("Vary order"),
    VARY_JUDGEMENT("Vary Judgement"),
    ADJOURN_OR_VACATE_HEARING("Adjourn or vacate a hearing"),
    OTHER("Other");
    private final String displayedValue;
}
