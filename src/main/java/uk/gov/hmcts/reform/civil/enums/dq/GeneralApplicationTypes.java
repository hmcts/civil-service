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
    SET_ASIDE_JUDGEMENT("Set aside judgement");

    private final String displayedValue;
}
