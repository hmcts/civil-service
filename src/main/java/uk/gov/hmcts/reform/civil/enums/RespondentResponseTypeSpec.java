package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentResponseTypeSpec {
    FULL_DEFENCE("Defends all of the claim"),
    FULL_ADMISSION("Admits all of the claim"),
    PART_ADMISSION("Admits part of the claim"),
    COUNTER_CLAIM("Reject all of the claim and wants to counterclaim");

    private final String displayedValue;
}
