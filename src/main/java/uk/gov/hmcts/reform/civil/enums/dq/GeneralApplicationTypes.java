package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneralApplicationTypes {
    STRIKE_OUT("Strike out"),
    SUMMARY_JUDGEMENT("Summary judgment"),
    STAY_THE_CLAIM("Stay the claim"),
    EXTEND_TIME("Extend time"),
    AMEND_A_STMT_OF_CASE("Amend a statement of case"),
    RELIEF_FROM_SANCTIONS("Relief from sanctions"),
    PROCEEDS_IN_HERITAGE("Proceeds In Heritage"),

    SET_ASIDE_JUDGEMENT("Set aside judgment"),
    VARY_ORDER("Vary order"),
    ADJOURN_VACATE_HEARING("Adjourn or vacate a hearing"),
    UNLESS_ORDER("Unless order"),
    OTHER("Other"),
    VARY_JUDGEMENT("Vary judgment");

    private final String displayedValue;
}
