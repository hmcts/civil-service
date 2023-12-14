package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;

@Getter
public enum ResponseIntention {
    FULL_DEFENCE("Defend all of the claim"),
    PART_DEFENCE("Defend part of the claim"),
    CONTEST_JURISDICTION("Contest the Court's jurisdiction");

    private final String label;

    ResponseIntention(String value) {
        this.label = value;
    }
}
