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
    AMEND_THE_STATEMENT_OF_CASE("Amend the statement of case"),
    RELIEF_FROM_SANCTIONS("Relief from sanctions");

    private final String displayedValue;
}
