package uk.gov.hmcts.reform.civil.enums.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyType {
    IND("IND"),
    ORG("ORG");

    private final String label;
}
