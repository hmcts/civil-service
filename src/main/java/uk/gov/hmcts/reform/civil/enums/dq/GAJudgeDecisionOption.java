package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAJudgeDecisionOption {
    MAKE_AN_ORDER("Make an order without a hearing"),
    FREE_FORM_ORDER("Free form order"),
    REQUEST_MORE_INFO("Request more information"),
    LIST_FOR_A_HEARING("List for a hearing"),
    MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS("Make an order for written representations");

    private final String displayedValue;
}
