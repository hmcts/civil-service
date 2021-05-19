package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentResponseType {
    FULL_DEFENCE("rejects all of the claim"),
    FULL_ADMISSION("admits all of the claim"),
    PART_ADMISSION("admits part of the claim"),
    COUNTER_CLAIM("rejects all of the claim and wants to counterclaim");

    private final String displayedValue;
}
