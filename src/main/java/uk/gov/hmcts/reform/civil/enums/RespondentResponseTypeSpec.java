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

    public RespondentResponseType translate() {
        switch (this) {
            case PART_ADMISSION:
                return RespondentResponseType.PART_ADMISSION;
            case FULL_ADMISSION:
                return RespondentResponseType.FULL_ADMISSION;
            case COUNTER_CLAIM:
                return RespondentResponseType.COUNTER_CLAIM;
            case FULL_DEFENCE:
                return RespondentResponseType.FULL_DEFENCE;
            default:
                return null;
        }
    }
}
