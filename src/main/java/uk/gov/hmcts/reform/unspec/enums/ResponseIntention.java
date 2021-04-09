package uk.gov.hmcts.reform.unspec.enums;

import lombok.Getter;

@Getter
public enum ResponseIntention {
    FULL_DEFENCE("Full defence"),
    PART_DEFENCE("Defend part of the claim"),
    CONTEST_JURISDICTION("Contest jurisdiction");

    private final String label;

    ResponseIntention(String value) {
        this.label = value;
    }
}
