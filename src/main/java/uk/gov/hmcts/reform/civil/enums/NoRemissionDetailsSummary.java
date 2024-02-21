package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;

@Getter
public enum NoRemissionDetailsSummary {
    NOT_QUALIFY_FEE_ASSISTANCE("Does not qualify for Help with Fees assistance"),
    INCORRECT_EVIDENCE("Incorrect evidence supplied"),
    INSUFFICIENT_EVIDENCE("Insufficient evidence supplied"),
    FEES_REQUIREMENT_NOT_MET("Income/outgoings calculation determines Help with Fees requirement not met");

    private final String label;

    NoRemissionDetailsSummary(String value) {
        this.label = value;
    }
}
